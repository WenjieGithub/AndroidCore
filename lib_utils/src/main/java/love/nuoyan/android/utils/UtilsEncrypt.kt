package love.nuoyan.android.utils

import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import java.util.zip.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object UtilsEncrypt {
    fun md5(array: ByteArray): String {
        val digest = MessageDigest.getInstance("MD5")
        val result = digest.digest(array)
        return toHex(result)                // 转成16进制后是32字节
    }

    /** md5加密字符串 32个字节 */
    fun md5(str: String): String {
        return md5(str.toByteArray(StandardCharsets.UTF_8))
    }

    /** 加盐的 md5 值. 这样即使被拖库，仍然可以有效抵御彩虹表攻击 */
    fun md5(buf: String, salt: String): String {
        return md5(md5(buf) + salt)
    }

    fun sha1(str: String): String {
        return sha1(str.toByteArray(StandardCharsets.UTF_8))
    }

    fun sha1(byteArray: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val result = digest.digest(byteArray)
        return toHex(result)
    }

    fun sha256(str: String): String {
        return sha256(str.toByteArray(StandardCharsets.UTF_8))
    }

    fun sha256(byteArray: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val result = digest.digest(byteArray)
        return toHex(result)
    }

    fun sha512(byteArray: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val result = digest.digest(byteArray)
        return toHex(result)
    }

    /**
     * DES 加密
     *
     * @param cleartext 明文
     * @param seed      密钥, 必须是 8 位
     * @return 密文
     */
    fun des(cleartext: String, seed: String): String? {
        return try {
            val key = SecretKeySpec(seed.toByteArray(), "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptedData = cipher.doFinal(cleartext.toByteArray())
            Base64.encodeToString(encryptedData, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun decryptDes(encrypted: String, seed: String): String? {
        return try {
            val key = SecretKeySpec(seed.toByteArray(), "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val byteMi = Base64.decode(encrypted, Base64.DEFAULT)
            val decryptedData = cipher.doFinal(byteMi)
            String(decryptedData)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * AES ECB 加密
     * @param src   需要加密的字符串
     * @param key   密匙，长度必须为 128/192/256 bits
     * @return      返回加密后密文，编码为 base64
     */
    fun aesECB(src: String, key: String): String? {
        return  aes(
            src.toByteArray(Charsets.UTF_8),
            key.toByteArray(Charsets.UTF_8),
            Cipher.ENCRYPT_MODE, "AES/ECB/PKCS5Padding")?.let {
            Base64.encodeToString(it, Base64.DEFAULT)
        }
    }
    fun aesDecryptECB(src: String, key: String): String? {
        val srcByteArray = if (src.contains("%")) {
            Base64.decode(URLDecoder.decode(src, Charsets.UTF_8.name()), Base64.DEFAULT)
        } else {
            Base64.decode(src, Base64.DEFAULT)
        }
        return aes(
            srcByteArray,
            key.toByteArray(Charsets.UTF_8),
            Cipher.DECRYPT_MODE, "AES/ECB/PKCS5Padding")?.toString(Charsets.UTF_8)
    }

    fun aes(src: String, key: String): String? {
        return aes(
            src.toByteArray(Charsets.UTF_8),
            md5(key, "lDA40Vix6HLzuNoM").toByteArray(Charsets.UTF_8),
            Cipher.ENCRYPT_MODE,
            "AES/CFB/NoPadding",
            "F759C22F5043614C"
        )?.let { toHex(it) }
    }
    fun aesDecrypt(src: String, key: String): String? {
        return aes(
            toByte(src),
            md5(key, "lDA40Vix6HLzuNoM").toByteArray(Charsets.UTF_8),
            Cipher.DECRYPT_MODE,
            "AES/CFB/NoPadding",
            "F759C22F5043614C"
        )?.toString(Charsets.UTF_8)
    }

    /**
     * @param src               加密的元数据
     * @param key               加密的密钥，长度必须为 128/192/256 bits
     * @param mode              加密或解密模式：Cipher.ENCRYPT_MODE、Cipher.DECRYPT_MODE
     * @param transformation    转换方式名称："AES/CFB/NoPadding"、"AES/ECB/PKCS5Padding"、
     * @param zeroIv            初始化向量，默认为空；有值长度必须为 128/192/256 bits
     * @return  字节数组或空
     */
    fun aes(src: ByteArray, key: ByteArray, mode: Int, transformation: String, zeroIv: String? = null): ByteArray? {
        return try {
            createCipher(key, mode, transformation, zeroIv)?.doFinal(src)
        } catch (e: Exception) {
            UtilsLog.log(e.stackTraceToString(), "UtilsEncryptAES")
            null
        }
    }
    fun createCipher(key: ByteArray, mode: Int, transformation: String, zeroIv: String? = null): Cipher? {
        return try {
            val sKeySpec = SecretKeySpec(key, "AES")
            Cipher.getInstance(transformation).apply {
                if (zeroIv == null) {
                    init(mode, sKeySpec)
                } else {
                    init(mode, sKeySpec, IvParameterSpec(zeroIv.toByteArray(Charsets.UTF_8)))
                }
            }
        } catch (e: Exception) {
            UtilsLog.log(e.stackTraceToString(), "UtilsEncryptAES")
            null
        }
    }


    // 转成16进制
    private fun toHex(byteArray: ByteArray): String {
        return with(StringBuilder()) {
            byteArray.forEach {
                val hex = it.toInt() and (0xFF)
                val hexStr = Integer.toHexString(hex).toUpperCase(Locale.ROOT)
                if (hexStr.length == 1) {
                    append("0").append(hexStr)
                } else {
                    append(hexStr)
                }
            }
            toString()
        }
    }
    // 将十六进制字符串转为十进制字节数组
    private fun toByte(hexString: String): ByteArray {
        val len = hexString.length / 2
        val result = ByteArray(len)
        for (i in 0 until len) {
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).toByte()
        }
        return result
    }

    /**
     * 压缩
     */
    fun zipString(str: String): String {
        /*
         * 0 ~ 9 压缩等级 低到高
         * public static final int BEST_COMPRESSION = 9;            最佳压缩的压缩级别。
         * public static final int BEST_SPEED = 1;                  压缩级别最快的压缩。
         * public static final int DEFAULT_COMPRESSION = -1;        默认压缩级别。
         * public static final int DEFAULT_STRATEGY = 0;            默认压缩策略。
         * public static final int DEFLATED = 8;                    压缩算法的压缩方法(目前唯一支持的压缩方法)。
         * public static final int FILTERED = 1;                    压缩策略最适用于大部分数值较小且数据分布随机分布的数据。
         * public static final int FULL_FLUSH = 3;                  压缩刷新模式，用于清除所有待处理的输出并重置拆卸器。
         * public static final int HUFFMAN_ONLY = 2;                仅用于霍夫曼编码的压缩策略。
         * public static final int NO_COMPRESSION = 0;              不压缩的压缩级别。
         * public static final int NO_FLUSH = 0;                    用于实现最佳压缩结果的压缩刷新模式。
         * public static final int SYNC_FLUSH = 2;                  用于清除所有未决输出的压缩刷新模式; 可能会降低某些压缩算法的压缩率。
         */
        if (str.isEmpty()) {
            return str
        }
        val deflate = Deflater(Deflater.BEST_COMPRESSION)          // 使用指定的压缩级别创建一个新的压缩器。
        val out = ByteArrayOutputStream(256)
        return try {
            deflate.setInput(str.toByteArray(Charsets.UTF_8))  // 设置压缩输入数据。
            deflate.finish()                                           // 当被调用时，表示压缩应该以输入缓冲区的当前内容结束。
            val bytes = ByteArray(256)
            while (!deflate.finished()) {                              // 压缩输入数据并用压缩数据填充指定的缓冲区。
                val length = deflate.deflate(bytes)
                out.write(bytes, 0, length)
            }
            Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            str
        } finally {
            deflate.end()
            out.close()
        }
    }

    /**
     * 解压缩
     */
    fun unzipString(str: String): String {
        if (str.isEmpty()) {
            return str
        }
        val inflater = Inflater()
        val out = ByteArrayOutputStream(256)
        return try {
            inflater.setInput(Base64.decode(str, Base64.DEFAULT))
            val bytes = ByteArray(256)
            while (!inflater.finished()) {
                val length: Int = inflater.inflate(bytes)
                out.write(bytes, 0, length)
            }
            out.toString(Charsets.UTF_8.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            str
        } finally {
            inflater.end()
            out.close()
        }
    }

    /**
     * @param input 需要压缩的字符串
     */
    fun gzipString(input: String): String {
        if (input.isEmpty()) {
            return input
        }
        val out = ByteArrayOutputStream()
        val gzipOs = GZIPOutputStream(out)
        gzipOs.write(input.toByteArray(Charsets.UTF_8))
        gzipOs.close()
        return Base64.encodeToString(out.toByteArray(), Base64.NO_PADDING)
    }

    /**
     * @param zippedStr 压缩后的字符串
     */
    fun unGzipString(zippedStr: String): String {
        if (zippedStr.isEmpty()) {
            return zippedStr
        }
        val decode = Base64.decode(zippedStr, Base64.NO_PADDING)
        val out = ByteArrayOutputStream()
        val inp = ByteArrayInputStream(decode)
        val gzipIs = GZIPInputStream(inp)
        val buffer = ByteArray(256)
        var n: Int
        while (gzipIs.read(buffer).also { n = it } >= 0) {
            out.write(buffer, 0, n)
        }
        return out.toString(Charsets.UTF_8.toString())
    }
}