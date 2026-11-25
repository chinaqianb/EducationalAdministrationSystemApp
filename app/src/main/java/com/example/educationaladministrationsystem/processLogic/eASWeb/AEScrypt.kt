package com.example.educationaladministrationsystem.processLogic.eASWeb

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object AEScrypt {
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128 // 认证标签长度
    private const val IV_LENGTH_BYTE = 12// GCM推荐的IV长度


    object PasswordCrypt{
        private const val PBKDF2_ITERATIONS = 65536
        private const val SALT_LENGTH = 16
        private const val IV_LENGTH = 16
        private const val AES_KEY_SIZE = 256 // 256位密钥
        private const val DELIMITER = ":"
        fun String.encryptPassword(): String{
            if (this.isNotEmpty()){
                return encryptAES(this)
            }
            return ""
        }
        fun String.decryptPassword(): String{
            if (this.isNotEmpty()){
                return decryptAES(this)
            }
            return ""
        }
        fun encryptAES(value: String, password: String="gtyf"): String {
            // 1. 生成随机盐
            val salt = ByteArray(SALT_LENGTH).apply {
                SecureRandom.getInstanceStrong().nextBytes(this)
            }

            // 2. 用 PBKDF2 从密码派生密钥
            val keySpec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_SIZE)
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val rawKey = keyFactory.generateSecret(keySpec).encoded
            val secretKey = SecretKeySpec(rawKey, "AES")

            // 3. 生成随机 IV
            val iv = ByteArray(IV_LENGTH).apply {
                SecureRandom.getInstanceStrong().nextBytes(this)
            }
            val ivSpec = IvParameterSpec(iv)

            // 4. 加密
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val encryptedBytes = cipher.doFinal(value.toByteArray(Charsets.UTF_8))

            // 5. 将盐、IV、密文分别 Base64 编码后用分隔符拼接
            val saltBase64 = Base64.getEncoder().encodeToString(salt)
            val ivBase64 = Base64.getEncoder().encodeToString(iv)
            val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes)

            return "$saltBase64$DELIMITER$ivBase64$DELIMITER$encryptedBase64"
        }

        /**
         * 扩展函数：解密
         * @param password 加密时使用的密码
         * @return 原始字符串
         */
        fun decryptAES(value: String, password: String="gtyf"): String {
            // 1. 拆分并 Base64 解码
            val parts = value.split(DELIMITER)
            require(parts.size == 3) { "密文格式错误" }
            val salt = Base64.getDecoder().decode(parts[0])
            val iv = Base64.getDecoder().decode(parts[1])
            val encryptedBytes = Base64.getDecoder().decode(parts[2])

            // 2. 用相同的盐和密码重新派生密钥
            val keySpec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_SIZE)
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val rawKey = keyFactory.generateSecret(keySpec).encoded
            val secretKey = SecretKeySpec(rawKey, "AES")

            // 3. 解密
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

            val plainBytes = cipher.doFinal(encryptedBytes)
            return String(plainBytes, Charsets.UTF_8)
        }

    }
    fun generateAESKey(): String {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return Base64.getEncoder().encodeToString(keyGen.generateKey().encoded)
    }
    fun String.encryptAES(): String{
        if (this.isNotEmpty()){
            return encrypt(this)
        }
        return ""
    }
    fun String.decryptAES(): String{
        if (this.isNotEmpty()){
            return decrypt(this)
        }
        return ""
    }
    /**
     * 加密字符串，返回 Base64 编码的 (IV + 密文)
     */
    fun encrypt(plainText: String, secretKey: String="FOddo23HD2+HAMabIaLvqlpQKU0bPMOr4WHPJthKGUo="): String {
        val cipher = Cipher.getInstance(AES_MODE)

        // 1. 生成随机IV（必须每次加密都不同）
        val iv = ByteArray(IV_LENGTH_BYTE)
        SecureRandom().nextBytes(iv)
        val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        val key= SecretKeySpec(Base64.getDecoder().decode(secretKey),"AES")
        // 2. 初始化为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)

        // 3. 执行加密
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // 4. 将 IV 和密文拼接后 Base64 编码（解密时需要 IV）
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

        return Base64.getEncoder().encodeToString(combined)
    }

    /**
     * 解密字符串
     */
    fun decrypt(encryptedBase64: String, secretKey: String="FOddo23HD2+HAMabIaLvqlpQKU0bPMOr4WHPJthKGUo="): String {
        // 1. Base64 解码
        val combined = Base64.getDecoder().decode(encryptedBase64)

        // 2. 分离 IV 和密文
        val iv = combined.copyOfRange(0, IV_LENGTH_BYTE)
        val cipherText = combined.copyOfRange(IV_LENGTH_BYTE, combined.size)
        val key= SecretKeySpec(Base64.getDecoder().decode(secretKey),"AES")
        // 3. 初始化为解密模式
        val cipher = Cipher.getInstance(AES_MODE)
        val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec)

        // 4. 解密
        val plainBytes = cipher.doFinal(cipherText)
        return String(plainBytes, Charsets.UTF_8)
    }
}