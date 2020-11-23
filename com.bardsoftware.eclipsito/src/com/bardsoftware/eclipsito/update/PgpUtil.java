/*
Copyright 2020 BarD Software s.r.o

This file is part of GanttProject, an opensource project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.bardsoftware.eclipsito.update;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Utilities for the verification of artifacts downloaded by the update system.
 *
 * The main entrance point for the updater is verifyFile. It assumes that the public key
 * is available as a classpath resource /bardsoftware.asc. It may be overridden with the
 * system property eclipsito.update.public_key
 *
 * @author dbarashev (Dmitry Barashev)
 */
public class PgpUtil {
  private static final Logger LOG = Logger.getLogger("Eclipsito.Update.Pgp");
  private static final KeyFingerPrintCalculator FP_CALC = new BcKeyFingerprintCalculator();
  //private static final BouncyCastleProvider provider = new BouncyCastleProvider();
  private static final Supplier<PGPPublicKey> ourPublicKey = () -> {
    var publicKeyResource = System.getProperty("eclipsito.update.public_key", "/bardsoftware.asc");
    var publicKeyStream = PgpUtil.class.getResourceAsStream(publicKeyResource);
    if (publicKeyStream == null) {
      var msg = String.format("Failed to read the public key from %s. This resource is missing.", publicKeyResource);
      LOG.severe(msg);
      throw new RuntimeException(msg);
    }
    PGPPublicKey publicKey;
    try {
      publicKey = readPublicKey(publicKeyStream);
      LOG.log(Level.FINE, String.format("Update system will use a public key %s", publicKeyToString(publicKey)));
      return publicKey;
    } catch (IOException | PGPException e) {
      var msg = String.format("Failed to read %s as a PGP public key.", publicKeyResource);
      LOG.log(Level.SEVERE, msg, e);
      throw  new RuntimeException(msg, e);
    }
  };

  static {
  //  Security.addProvider(provider);
  }

  private static PGPPublicKey readPublicKey(InputStream keyStream) throws IOException, PGPException {
    PGPPublicKeyRingCollection pkCol = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(keyStream), FP_CALC);
    Iterator<PGPPublicKeyRing> it = pkCol.getKeyRings();
    while (it.hasNext()) {
      PGPPublicKeyRing pkRing =  it.next();
      Iterator<PGPPublicKey> pkIt = pkRing.getPublicKeys();
      while (pkIt.hasNext()) {
        PGPPublicKey key = pkIt.next();
        if (key.isEncryptionKey()) {
          return key;
        }
      }
    }
    return null;
  }

  private static String publicKeyToString(PGPPublicKey publicKey) {
    var info = publicKeyInfo(publicKey);
    return String.format("%n%s%n%s", info.uid, info.keyFingerprint);
  }

  private static PgpInfo publicKeyInfo(PGPPublicKey publicKey) {
    var ids = new ArrayList<String>();
    publicKey.getUserIDs().forEachRemaining(ids::add);
    StringBuilder fingerprintString = new StringBuilder();
    for (byte b : publicKey.getFingerprint()) {
      fingerprintString.append(String.format("%02X", b));
    }
    return new PgpInfo(String.join("\n", ids), fingerprintString.toString(), null);
  }

  private static <T> T find(List<Object> pgpObjects, Class<T> clazz) {
    for (Object o : pgpObjects) {
      if (o.getClass().isAssignableFrom(clazz)) {
        return (T) o;
      }
    }
    return null;
  }

  static PGPSignature getSignature(PGPPublicKey publicKey, InputStream sigStream) throws PgpUtil.Exception {
    try (InputStream in = PGPUtil.getDecoderStream(sigStream)) {
      PGPObjectFactory pgpFact = new JcaPGPObjectFactory(in);

      List<Object> pgpObjects = new ArrayList<>();
      pgpFact.forEach(pgpObjects::add);

      PGPSignatureList sigList = find(pgpObjects, PGPSignatureList.class);
      if (sigList == null) {
        throw new RuntimeException("Failed to verify PGP signature: siglist not found");
      }

      PGPSignature signature = sigList.get(0);
      signature.init(new JcaPGPContentVerifierBuilderProvider(), publicKey);
      return signature;
    } catch (Exception | PGPException | IOException e) {
      var msg = "Failed to read the signature.";
      LOG.log(Level.SEVERE, msg, e);
      throw new PgpUtil.Exception(msg, e);
    }
  }

  static class PgpInfo {
    final String uid;
    final String keyFingerprint;
    final Date signatureCreationTime;

    PgpInfo(String uid, String keyFingerprint, Date signatureCreationTime) {
      this.uid = uid;
      this.keyFingerprint = keyFingerprint;
      this.signatureCreationTime = signatureCreationTime;
    }
  }

  /**
   * Verifies data file PGP signature.
   *
   * @param dataFile Data file to be verified
   * @param signatureFile Signature file
   * @param publicKey public key for the verification or null if the default public key shall be used
   * @return PgpInfo instance if verification was successfull. Throws PgpUtil.Exception otherwise.
   * @throws PgpUtil.Exception in case of internal errors or verification failure
   */
  static PgpInfo verifyFile(File dataFile, File signatureFile, PGPPublicKey publicKey) throws PgpUtil.Exception {
    if (publicKey == null) {
      publicKey = ourPublicKey.get();
    }
    try {
      var signature = getSignature(publicKey,
          new BufferedInputStream(new FileInputStream(signatureFile)));
      signature.update(new FileInputStream(dataFile).readAllBytes());

      if (signature.verify() && signature.getKeyID() == publicKey.getKeyID()) {
        var keyInfo = publicKeyInfo(publicKey);
        return new PgpInfo(keyInfo.uid, keyInfo.keyFingerprint, signature.getCreationTime());
      } else {
        var msg = "Verification failed because the signature and the public key do no match.";
        throw new PgpUtil.Exception(msg);
      }
    } catch (IOException | PGPException e) {
      var msg = "Verification failed because of the internal error.";
      LOG.log(Level.SEVERE, msg, e);
      throw new PgpUtil.Exception(msg, e);
    }
  }

  /**
   * Utility main function for quick verifications.
   */
  public static void main(String[] argv) throws IOException, PGPException {
    Args args = new Args();
    JCommander parser = JCommander.newBuilder().addObject(args).build();
    parser.parse(argv);

    var pgpPublicKey = readPublicKey(new BufferedInputStream(new FileInputStream(args.publicKey)));
    var pgpInfo = verifyFile(new File(args.inputFile), new File(args.sigFile), pgpPublicKey);
    System.out.printf(
        "OK%nSigned by: %s%nSigned at: %s%nFingerprint: %s%n",
        pgpInfo.uid, pgpInfo.signatureCreationTime, pgpInfo.keyFingerprint
    );
  }

  static class Args {
    @Parameter(names = "--key", description = "Public key file")
    public String publicKey = "";

    @Parameter(names = "--data", description = "Input file")
    public String inputFile = "";

    @Parameter(names = "--sig", description = "Signature file")
    public String sigFile = "";

  }

  static class Exception extends RuntimeException {
    public Exception(String msg, Throwable cause) {
      super(msg, cause);
    }

    public Exception(String msg) {
      super(msg);
    }
  }
}
