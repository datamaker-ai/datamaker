/*
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

package ai.datamaker.sink.filter;

import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.utils.crypto.PgpHelper;
import ai.datamaker.utils.crypto.RSAKeyPairGenerator;
import ai.datamaker.utils.stream.ClosingWrapperOutputStream;
import com.google.common.collect.Lists;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Support PGP encryption...
 *
 * https://docs.oracle.com/javase/9/docs/specs/security/standard-names.html#secretkeyfactory-algorithms
 */
public class EncryptFilter {

    public static final PropertyConfig PGP_PUBLIC_KEY_PATH
            = new PropertyConfig("encrypt.filter.pgp.public.key",
                                 "PGP public key path",
                                 PropertyConfig.ValueType.STRING,
                                 "",
                                 Collections.emptyList());

    public static final PropertyConfig PGP_ARMORED
            = new PropertyConfig("encrypt.filter.pgp.armored",
                                 "PGP armored",
                                 PropertyConfig.ValueType.BOOLEAN,
                                 false,
                                 Collections.emptyList());

    public static final PropertyConfig ENCRYPTION_ALGORITHM
            = new PropertyConfig("encrypt.filter.algorithm",
                                 "Encryption algorithm",
                                 PropertyConfig.ValueType.STRING,
                                 "NONE",
                                 Lists.newArrayList("BCRYPT", "NONE", "PGP"));

    private static final SecureRandom RANDOM = new SecureRandom();

    public static List<PropertyConfig> addDefaultProperties(List<PropertyConfig> properties) {
        properties.add(ENCRYPTION_ALGORITHM);
        properties.add(PGP_PUBLIC_KEY_PATH);
        properties.add(PGP_ARMORED);
        return properties;
    }
    /**
     * Mode	Encrypt
     * Encrypt
     * Decrypt
     * Specifies whether the content should be encrypted or decrypted
     * Key Derivation Function	BCRYPT
     * NiFi Legacy KDF MD5 @ 1000 iterations
     * OpenSSL EVP_BytesToKey Single iteration MD5 compatible with PKCS#5 v1.5
     * Bcrypt Bcrypt with configurable work factor. See Admin Guide
     * Scrypt Scrypt with configurable cost parameters. See Admin Guide
     * PBKDF2 PBKDF2 with configurable hash function and iteration count. See Admin Guide
     * None The cipher is given a raw key conforming to the algorithm specifications
     * Specifies the key derivation function to generate the key from the password (and salt)
     * Encryption Algorithm	MD5_128AES
     * MD5_128AES org.apache.nifi.security.util.EncryptionMethod@5dc3fcb7[Algorithm name=PBEWITHMD5AND128BITAES-CBC-OPENSSL,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * MD5_192AES EncryptionMethod[Algorithm name=PBEWITHMD5AND192BITAES-CBC-OPENSSL,Requires unlimited strength JCE policy=true,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * MD5_256AES EncryptionMethod[Algorithm name=PBEWITHMD5AND256BITAES-CBC-OPENSSL,Requires unlimited strength JCE policy=true,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * MD5_DES EncryptionMethod[Algorithm name=PBEWITHMD5ANDDES,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * MD5_RC2 EncryptionMethod[Algorithm name=PBEWITHMD5ANDRC2,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA1_RC2 EncryptionMethod[Algorithm name=PBEWITHSHA1ANDRC2,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA1_DES EncryptionMethod[Algorithm name=PBEWITHSHA1ANDDES,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA_128AES EncryptionMethod[Algorithm name=PBEWITHSHAAND128BITAES-CBC-BC,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA_192AES EncryptionMethod[Algorithm name=PBEWITHSHAAND192BITAES-CBC-BC,Requires unlimited strength JCE policy=true,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA_256AES EncryptionMethod[Algorithm name=PBEWITHSHAAND256BITAES-CBC-BC,Requires unlimited strength JCE policy=true,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA_40RC2 EncryptionMethod[Algorithm name=PBEWITHSHAAND40BITRC2-CBC,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA_128RC2 EncryptionMethod[Algorithm name=PBEWITHSHAAND128BITRC2-CBC,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA_40RC4 EncryptionMethod[Algorithm name=PBEWITHSHAAND40BITRC4,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA_128RC4 EncryptionMethod[Algorithm name=PBEWITHSHAAND128BITRC4,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA256_128AES EncryptionMethod[Algorithm name=PBEWITHSHA256AND128BITAES-CBC-BC,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA256_192AES EncryptionMethod[Algorithm name=PBEWITHSHA256AND192BITAES-CBC-BC,Requires unlimited strength JCE policy=true,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA256_256AES EncryptionMethod[Algorithm name=PBEWITHSHA256AND256BITAES-CBC-BC,Requires unlimited strength JCE policy=true,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA_2KEYTRIPLEDES EncryptionMethod[Algorithm name=PBEWITHSHAAND2-KEYTRIPLEDES-CBC,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA_3KEYTRIPLEDES EncryptionMethod[Algorithm name=PBEWITHSHAAND3-KEYTRIPLEDES-CBC,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * SHA_TWOFISH EncryptionMethod[Algorithm name=PBEWITHSHAANDTWOFISH-CBC,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * PGP EncryptionMethod[Algorithm name=PGP,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * PGP_ASCII_ARMOR EncryptionMethod[Algorithm name=PGP-ASCII-ARMOR,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=false,Keyed cipher=false]
     * AES_CBC EncryptionMethod[Algorithm name=AES/CBC/PKCS7Padding,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=true,Keyed cipher=true]
     * AES_CTR EncryptionMethod[Algorithm name=AES/CTR/NoPadding,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=true,Keyed cipher=true]
     * AES_GCM EncryptionMethod[Algorithm name=AES/GCM/NoPadding,Requires unlimited strength JCE policy=false,Algorithm Provider=BC,Compatible with strong KDFs=true,Keyed cipher=true]
     * The Encryption Algorithm to use
     * Allow insecure cryptographic modes	not-allowed
     * Allowed Operation will not be blocked and no alerts will be presented when unsafe combinations of encryption algorithms and passwords are provided
     * Not Allowed When set, operation will be blocked and alerts will be presented to the user if unsafe combinations of encryption algorithms and passwords are provided on a JVM with limited strength crypto. To fix this, see the Admin Guide.
     * Overrides the default behavior to prevent unsafe combinations of encryption algorithms and short passwords on JVMs with limited strength cryptographic jurisdiction policies
     * Password			The Password to use for encrypting or decrypting the data
     * Sensitive Property: true
     * Raw Key (hexadecimal)			In keyed encryption, this is the raw key, encoded in hexadecimal
     * Sensitive Property: true
     * Public Keyring File			In a PGP encrypt mode, this keyring contains the public key of the recipient
     * Public Key User Id			In a PGP encrypt mode, this user id of the recipient
     * Private Keyring File			In a PGP decrypt mode, this keyring contains the private key of the recipient
     * Private Keyring Passphrase			In a PGP decrypt mode, this is the private keyring passphrase
     * Sensitive Property: true
     */

    /**
     * AES 	Constructs secret keys for use with the AES algorithm.
     * ARCFOUR 	Constructs secret keys for use with the ARCFOUR algorithm.
     * DES 	Constructs secrets keys for use with the DES algorithm.
     * DESede 	Constructs secrets keys for use with the DESede (Triple-DES) algorithm.
     * PBEWith<digest>And<encryption>
     * PBEWith<prf>And<encryption> 	Secret-key factory for use with PKCS5 password-based encryption, where <digest> is a message digest, <prf> is a pseudo-random function, and <encryption> is an encryption algorithm.
     *
     * Examples:
     *
     *     PBEWithMD5AndDES (PKCS5, v 1.5),
     *     PBEWithHmacSHA256AndAES_128 (PKCS5, v 2.0), and
     *
     * Note: These all use only the low order 8 bits of each password character.
     * PBKDF2WithHmacSHA1 	Constructs secret keys using the Password-Based Key Derivation Function function found in PKCS #5 v2.0.
     *
     */

    /**
     * AES 	Key generator for use with the AES algorithm.
     * ARCFOUR 	Key generator for use with the ARCFOUR (RC4) algorithm.
     * Blowfish 	Key generator for use with the Blowfish algorithm.
     * DES 	Key generator for use with the DES algorithm.
     * DESede 	Key generator for use with the DESede (triple-DES) algorithm.
     * HmacMD5 	Key generator for use with the HmacMD5 algorithm.
     * HmacSHA1 HmacSHA256 HmacSHA384 HmacSHA512 	Keys generator for use with the various flavors of the HmacSHA algorithms.
     * RC2 	Key generator for use with the RC2 algorithm.
     */

    private static final int DEFAULT_MAX_ALLOWED_KEY_LENGTH = 128;
    private static final int MINIMUM_SAFE_PASSWORD_LENGTH = 10;

    private static boolean isUnlimitedStrengthCryptographyEnabled;

    // Evaluate an unlimited strength algorithm to determine if we support the capability we have on the system
    static {
        try {
            isUnlimitedStrengthCryptographyEnabled = (Cipher.getMaxAllowedKeyLength("AES") > DEFAULT_MAX_ALLOWED_KEY_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            // if there are issues with this, we default back to the value established
            isUnlimitedStrengthCryptographyEnabled = false;
        }
    }

    private static boolean isArmored = false;
    private static String id = "damico";
    private static String passwd = "******";
    private static boolean integrityCheck = true;

    private static String pubKeyFile = "/tmp/pub.dat";
    private static String privKeyFile = "/tmp/secret.dat";

    public static void main(String[] args) throws Exception {

        RSAKeyPairGenerator rkpg = new RSAKeyPairGenerator();
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        FileOutputStream out1 = new FileOutputStream(privKeyFile);
        FileOutputStream out2 = new FileOutputStream(pubKeyFile);
        rkpg.exportKeyPair(out1, out2, kp.getPublic(), kp.getPrivate(), id, passwd.toCharArray(), isArmored);
    }

    public static OutputStream encryptStream(JobConfig config, OutputStream outputStream) throws Exception {
        String encryptAlgorithm = (String) config.getConfigProperty(EncryptFilter.ENCRYPTION_ALGORITHM);

        switch (encryptAlgorithm) {
            case "BCRYPT":
            case "NONE":
                return outputStream;
            case "PGP":
                String pubKeyFile = (String) config.getConfigProperty(EncryptFilter.PGP_PUBLIC_KEY_PATH);
                boolean armored = (boolean) config.getConfigProperty(EncryptFilter.PGP_ARMORED);
                return getPgpEncryptedStream(outputStream, pubKeyFile, armored);
        }
        return outputStream;
    }

    // TODO add recipients, configurable cipher
    public static OutputStream getPgpEncryptedStream(OutputStream outputStream, String pubKeyFile, boolean isArmored) throws Exception {
        OutputStream literalOut = null;
        OutputStream encryptedOut = null;
        OutputStream compressedOut = null;
        OutputStream armoredOut = outputStream;
        int bufferSize = 1024;

        if (isArmored) {
            armoredOut = new ArmoredOutputStream(outputStream);
        }
        BcPGPDataEncryptorBuilder dataEncryptor = new BcPGPDataEncryptorBuilder(PGPEncryptedData.AES_256);
        dataEncryptor.setSecureRandom(new SecureRandom());
        dataEncryptor.setWithIntegrityPacket(true);
        PGPEncryptedDataGenerator encryptGen = new PGPEncryptedDataGenerator(dataEncryptor);

        InputStream publicKeyStream = new FileInputStream(pubKeyFile);;
        PGPPublicKey publicKey = PgpHelper.getInstance().readPublicKey(publicKeyStream);

        if (publicKey == null) {
            throw new IllegalArgumentException("Couldn't obtain public key.");
        }

        encryptGen.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(publicKey));

        encryptedOut = encryptGen.open(armoredOut, new byte[bufferSize]);

//        PGPCompressedDataGenerator compressGen = new PGPCompressedDataGenerator(PGPCompressedData.UNCOMPRESSED);
//        compressedOut = compressGen.open(encryptedOut);

        PGPLiteralDataGenerator literalGen = new PGPLiteralDataGenerator();
        literalOut = literalGen.open(encryptedOut,
                                     PGPLiteralDataGenerator.UTF8,
                                     "Response",
                                     new Date(),
                                     new byte[bufferSize]);
        // return encryptedOut;
        return new ClosingWrapperOutputStream(literalOut, encryptedOut);
    }

    public static OutputStream getPGPEncryptedStream(OutputStream output, String pubKeyFile) throws Exception {

        // TODO add senders, ...

        FileInputStream pubKeyIs = new FileInputStream(pubKeyFile);
        Security.addProvider(new BouncyCastleProvider());

        if (isArmored) {
            output = new ArmoredOutputStream(output);
        }

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);

//        org.bouncycastle.openpgp.PGPUtil.writeFileToLiteralData(comData.open(bOut),
//                                                                PGPLiteralData.BINARY,
//                                                                new File(fileName));
//        comData.close();

        // new JcePGPDataEncryptorBuilder(encAlgorithm).setWithIntegrityPacket(withIntegrityPacket).setSecureRandom(rand).setProvider(provider)
        PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(PGPEncryptedData.CAST5, integrityCheck, new SecureRandom(), "BC");

        cPk.addMethod(PgpHelper.getInstance().readPublicKey(pubKeyIs));

        byte[] bytes = bOut.toByteArray();

        OutputStream cOut = cPk.open(comData.open(output), new byte[10]);

        //cOut.write(bytes);

        //cOut.close();

        // out.close();

        return cOut;
    }

    public static String generateSecretKey(String algorithm) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);

        SecureRandom secureRandom = new SecureRandom(new byte[]{1, 2, 3});
        int keyBitSize = 256;
        keyGenerator.init(keyBitSize, secureRandom);

        SecretKey secretKey = keyGenerator.generateKey();

        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        // return new String(secretKey.getEncoded());
    }

    public static OutputStream getKeyEncryptedStream(String key, String algorithm) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);

        return OutputStream.nullOutputStream();
    }

    /**
     * SecretKeyFactory Algorithms
     *
     * The following algorithm names can be specified when requesting an instance of SecretKeyFactory.
     * Algorithm Name 	Description
     * AES 	Constructs secret keys for use with the AES algorithm.
     * ARCFOUR 	Constructs secret keys for use with the ARCFOUR algorithm.
     * DES 	Constructs secrets keys for use with the DES algorithm.
     * DESede 	Constructs secrets keys for use with the DESede (Triple-DES) algorithm.
     * PBEWith<digest>And<encryption>
     * PBEWith<prf>And<encryption> 	Secret-key factory for use with PKCS5 password-based encryption, where <digest> is a message digest, <prf> is a pseudo-random function, and <encryption> is an encryption algorithm. Examples:
     *
     * PBEWithMD5AndDES (PKCS #5, 1.5),
     * PBEWithHmacSHA256AndAES_128 (PKCS #5, 2.0)
     *
     * Note: These all use only the low order 8 bits of each password character.
     * PBKDF2With<prf> 	Password-based key-derivation algorithm found in PKCS #5 2.0 using the specified pseudo-random function (<prf>). Example:
     * PBKDF2WithHmacSHA256.
     *
     * @param password
     * @param salt
     * @param keyLength
     * @return
     * @throws Exception
     */
    public static OutputStream getPasswordEncryptedStream(OutputStream output, String password, String salt, int keyLength) throws Exception {
        byte[] bytesIV = new byte[16];
        RANDOM.nextBytes(bytesIV);

        /* KEY + IV setting */
        IvParameterSpec iv = new IvParameterSpec(bytesIV);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, keyLength);
        SecretKey secretKey = factory.generateSecret(spec);
        //SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        final Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        return new CipherOutputStream(output, c);
    }

    public static OutputStream getEncryptedStream(OutputStream output) throws Exception {

        final KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(new SecureRandom(new byte[]{1, 2, 3}));
        final SecretKey key = kg.generateKey();
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println(encodedKey);

        final Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        return new CipherOutputStream(output, c);
    }

    void encrypt(String content, String fileName) throws Exception {
        SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();
        Cipher cipher = Cipher.getInstance("transformation");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] iv = cipher.getIV();

        try (FileOutputStream fileOut = new FileOutputStream(fileName); CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher)) {
            fileOut.write(iv);
            cipherOut.write(content.getBytes());
        }
    }
}
