begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.settings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
package|;
end_package

begin_import
import|import
name|javax
operator|.
name|crypto
operator|.
name|SecretKey
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|crypto
operator|.
name|SecretKeyFactory
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|crypto
operator|.
name|spec
operator|.
name|PBEKeySpec
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|DestroyFailedException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|CharBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharsetEncoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|StandardCopyOption
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|attribute
operator|.
name|PosixFileAttributeView
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|attribute
operator|.
name|PosixFilePermissions
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|GeneralSecurityException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|KeyStore
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|KeyStoreException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|NoSuchAlgorithmException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Enumeration
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|codecs
operator|.
name|CodecUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|BufferedChecksumIndexInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|ChecksumIndexInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|IOContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|IndexInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|IndexOutput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|SimpleFSDirectory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|SetOnce
import|;
end_import

begin_comment
comment|/**  * A wrapper around a Java KeyStore which provides supplements the keystore with extra metadata.  *  * Loading a keystore has 2 phases. First, call {@link #load(Path)}. Then call  * {@link #decrypt(char[])} with the keystore password, or an empty char array if  * {@link #hasPassword()} is {@code false}.  Loading and decrypting should happen  * in a single thread. Once decrypted, keys may be read with the wrapper in  * multiple threads.  */
end_comment

begin_class
DECL|class|KeyStoreWrapper
specifier|public
class|class
name|KeyStoreWrapper
implements|implements
name|Closeable
block|{
comment|/** The name of the keystore file to read and write. */
DECL|field|KEYSTORE_FILENAME
specifier|private
specifier|static
specifier|final
name|String
name|KEYSTORE_FILENAME
init|=
literal|"elasticsearch.keystore"
decl_stmt|;
comment|/** The version of the metadata written before the keystore data. */
DECL|field|FORMAT_VERSION
specifier|private
specifier|static
specifier|final
name|int
name|FORMAT_VERSION
init|=
literal|1
decl_stmt|;
comment|/** The keystore type for a newly created keystore. */
DECL|field|NEW_KEYSTORE_TYPE
specifier|private
specifier|static
specifier|final
name|String
name|NEW_KEYSTORE_TYPE
init|=
literal|"PKCS12"
decl_stmt|;
comment|/** The algorithm used to store password for a newly created keystore. */
DECL|field|NEW_KEYSTORE_SECRET_KEY_ALGO
specifier|private
specifier|static
specifier|final
name|String
name|NEW_KEYSTORE_SECRET_KEY_ALGO
init|=
literal|"PBE"
decl_stmt|;
comment|//"PBEWithHmacSHA256AndAES_128";
comment|/** An encoder to check whether string values are ascii. */
DECL|field|ASCII_ENCODER
specifier|private
specifier|static
specifier|final
name|CharsetEncoder
name|ASCII_ENCODER
init|=
name|StandardCharsets
operator|.
name|US_ASCII
operator|.
name|newEncoder
argument_list|()
decl_stmt|;
comment|/** True iff the keystore has a password needed to read. */
DECL|field|hasPassword
specifier|private
specifier|final
name|boolean
name|hasPassword
decl_stmt|;
comment|/** The type of the keystore, as passed to {@link java.security.KeyStore#getInstance(String)} */
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
comment|/** A factory necessary for constructing instances of secrets in a {@link KeyStore}. */
DECL|field|secretFactory
specifier|private
specifier|final
name|SecretKeyFactory
name|secretFactory
decl_stmt|;
comment|/** The raw bytes of the encrypted keystore. */
DECL|field|keystoreBytes
specifier|private
specifier|final
name|byte
index|[]
name|keystoreBytes
decl_stmt|;
comment|/** The loaded keystore. See {@link #decrypt(char[])}. */
DECL|field|keystore
specifier|private
specifier|final
name|SetOnce
argument_list|<
name|KeyStore
argument_list|>
name|keystore
init|=
operator|new
name|SetOnce
argument_list|<>
argument_list|()
decl_stmt|;
comment|/** The password for the keystore. See {@link #decrypt(char[])}. */
DECL|field|keystorePassword
specifier|private
specifier|final
name|SetOnce
argument_list|<
name|KeyStore
operator|.
name|PasswordProtection
argument_list|>
name|keystorePassword
init|=
operator|new
name|SetOnce
argument_list|<>
argument_list|()
decl_stmt|;
comment|/** The setting names contained in the loaded keystore. */
DECL|field|settingNames
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|settingNames
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|KeyStoreWrapper
specifier|private
name|KeyStoreWrapper
parameter_list|(
name|boolean
name|hasPassword
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|secretKeyAlgo
parameter_list|,
name|byte
index|[]
name|keystoreBytes
parameter_list|)
block|{
name|this
operator|.
name|hasPassword
operator|=
name|hasPassword
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
try|try
block|{
name|secretFactory
operator|=
name|SecretKeyFactory
operator|.
name|getInstance
argument_list|(
name|secretKeyAlgo
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|this
operator|.
name|keystoreBytes
operator|=
name|keystoreBytes
expr_stmt|;
block|}
comment|/** Returns a path representing the ES keystore in the given config dir. */
DECL|method|keystorePath
specifier|static
name|Path
name|keystorePath
parameter_list|(
name|Path
name|configDir
parameter_list|)
block|{
return|return
name|configDir
operator|.
name|resolve
argument_list|(
name|KEYSTORE_FILENAME
argument_list|)
return|;
block|}
comment|/** Constructs a new keystore with the given password. */
DECL|method|create
specifier|static
name|KeyStoreWrapper
name|create
parameter_list|(
name|char
index|[]
name|password
parameter_list|)
throws|throws
name|Exception
block|{
name|KeyStoreWrapper
name|wrapper
init|=
operator|new
name|KeyStoreWrapper
argument_list|(
name|password
operator|.
name|length
operator|!=
literal|0
argument_list|,
name|NEW_KEYSTORE_TYPE
argument_list|,
name|NEW_KEYSTORE_SECRET_KEY_ALGO
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|KeyStore
name|keyStore
init|=
name|KeyStore
operator|.
name|getInstance
argument_list|(
name|NEW_KEYSTORE_TYPE
argument_list|)
decl_stmt|;
name|keyStore
operator|.
name|load
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|wrapper
operator|.
name|keystore
operator|.
name|set
argument_list|(
name|keyStore
argument_list|)
expr_stmt|;
name|wrapper
operator|.
name|keystorePassword
operator|.
name|set
argument_list|(
operator|new
name|KeyStore
operator|.
name|PasswordProtection
argument_list|(
name|password
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|wrapper
return|;
block|}
comment|/**      * Loads information about the Elasticsearch keystore from the provided config directory.      *      * {@link #decrypt(char[])} must be called before reading or writing any entries.      * Returns {@code null} if no keystore exists.      */
DECL|method|load
specifier|public
specifier|static
name|KeyStoreWrapper
name|load
parameter_list|(
name|Path
name|configDir
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|keystoreFile
init|=
name|keystorePath
argument_list|(
name|configDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|keystoreFile
argument_list|)
operator|==
literal|false
condition|)
block|{
return|return
literal|null
return|;
block|}
name|SimpleFSDirectory
name|directory
init|=
operator|new
name|SimpleFSDirectory
argument_list|(
name|configDir
argument_list|)
decl_stmt|;
try|try
init|(
name|IndexInput
name|indexInput
init|=
name|directory
operator|.
name|openInput
argument_list|(
name|KEYSTORE_FILENAME
argument_list|,
name|IOContext
operator|.
name|READONCE
argument_list|)
init|)
block|{
name|ChecksumIndexInput
name|input
init|=
operator|new
name|BufferedChecksumIndexInput
argument_list|(
name|indexInput
argument_list|)
decl_stmt|;
name|CodecUtil
operator|.
name|checkHeader
argument_list|(
name|input
argument_list|,
name|KEYSTORE_FILENAME
argument_list|,
name|FORMAT_VERSION
argument_list|,
name|FORMAT_VERSION
argument_list|)
expr_stmt|;
name|byte
name|hasPasswordByte
init|=
name|input
operator|.
name|readByte
argument_list|()
decl_stmt|;
name|boolean
name|hasPassword
init|=
name|hasPasswordByte
operator|==
literal|1
decl_stmt|;
if|if
condition|(
name|hasPassword
operator|==
literal|false
operator|&&
name|hasPasswordByte
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"hasPassword boolean is corrupt: "
operator|+
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"%02x"
argument_list|,
name|hasPasswordByte
argument_list|)
argument_list|)
throw|;
block|}
name|String
name|type
init|=
name|input
operator|.
name|readString
argument_list|()
decl_stmt|;
name|String
name|secretKeyAlgo
init|=
name|input
operator|.
name|readString
argument_list|()
decl_stmt|;
name|byte
index|[]
name|keystoreBytes
init|=
operator|new
name|byte
index|[
name|input
operator|.
name|readInt
argument_list|()
index|]
decl_stmt|;
name|input
operator|.
name|readBytes
argument_list|(
name|keystoreBytes
argument_list|,
literal|0
argument_list|,
name|keystoreBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|CodecUtil
operator|.
name|checkFooter
argument_list|(
name|input
argument_list|)
expr_stmt|;
return|return
operator|new
name|KeyStoreWrapper
argument_list|(
name|hasPassword
argument_list|,
name|type
argument_list|,
name|secretKeyAlgo
argument_list|,
name|keystoreBytes
argument_list|)
return|;
block|}
block|}
comment|/** Returns true iff {@link #decrypt(char[])} has been called. */
DECL|method|isLoaded
specifier|public
name|boolean
name|isLoaded
parameter_list|()
block|{
return|return
name|keystore
operator|.
name|get
argument_list|()
operator|!=
literal|null
return|;
block|}
comment|/** Return true iff calling {@link #decrypt(char[])} requires a non-empty password. */
DECL|method|hasPassword
specifier|public
name|boolean
name|hasPassword
parameter_list|()
block|{
return|return
name|hasPassword
return|;
block|}
comment|/**      * Decrypts the underlying java keystore.      *      * This may only be called once. The provided password will be zeroed out.      */
DECL|method|decrypt
specifier|public
name|void
name|decrypt
parameter_list|(
name|char
index|[]
name|password
parameter_list|)
throws|throws
name|GeneralSecurityException
throws|,
name|IOException
block|{
if|if
condition|(
name|keystore
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Keystore has already been decrypted"
argument_list|)
throw|;
block|}
name|keystore
operator|.
name|set
argument_list|(
name|KeyStore
operator|.
name|getInstance
argument_list|(
name|type
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|InputStream
name|in
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|keystoreBytes
argument_list|)
init|)
block|{
name|keystore
operator|.
name|get
argument_list|()
operator|.
name|load
argument_list|(
name|in
argument_list|,
name|password
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|keystoreBytes
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
block|}
name|keystorePassword
operator|.
name|set
argument_list|(
operator|new
name|KeyStore
operator|.
name|PasswordProtection
argument_list|(
name|password
argument_list|)
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|password
argument_list|,
literal|'\0'
argument_list|)
expr_stmt|;
comment|// convert keystore aliases enum into a set for easy lookup
name|Enumeration
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|keystore
operator|.
name|get
argument_list|()
operator|.
name|aliases
argument_list|()
decl_stmt|;
while|while
condition|(
name|aliases
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|settingNames
operator|.
name|add
argument_list|(
name|aliases
operator|.
name|nextElement
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Write the keystore to the given config directory. */
DECL|method|save
name|void
name|save
parameter_list|(
name|Path
name|configDir
parameter_list|)
throws|throws
name|Exception
block|{
name|char
index|[]
name|password
init|=
name|this
operator|.
name|keystorePassword
operator|.
name|get
argument_list|()
operator|.
name|getPassword
argument_list|()
decl_stmt|;
name|SimpleFSDirectory
name|directory
init|=
operator|new
name|SimpleFSDirectory
argument_list|(
name|configDir
argument_list|)
decl_stmt|;
comment|// write to tmp file first, then overwrite
name|String
name|tmpFile
init|=
name|KEYSTORE_FILENAME
operator|+
literal|".tmp"
decl_stmt|;
try|try
init|(
name|IndexOutput
name|output
init|=
name|directory
operator|.
name|createOutput
argument_list|(
name|tmpFile
argument_list|,
name|IOContext
operator|.
name|DEFAULT
argument_list|)
init|)
block|{
name|CodecUtil
operator|.
name|writeHeader
argument_list|(
name|output
argument_list|,
name|KEYSTORE_FILENAME
argument_list|,
name|FORMAT_VERSION
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeByte
argument_list|(
name|password
operator|.
name|length
operator|==
literal|0
condition|?
operator|(
name|byte
operator|)
literal|0
else|:
operator|(
name|byte
operator|)
literal|1
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeString
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeString
argument_list|(
name|secretFactory
operator|.
name|getAlgorithm
argument_list|()
argument_list|)
expr_stmt|;
name|ByteArrayOutputStream
name|keystoreBytesStream
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|keystore
operator|.
name|get
argument_list|()
operator|.
name|store
argument_list|(
name|keystoreBytesStream
argument_list|,
name|password
argument_list|)
expr_stmt|;
name|byte
index|[]
name|keystoreBytes
init|=
name|keystoreBytesStream
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|output
operator|.
name|writeInt
argument_list|(
name|keystoreBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeBytes
argument_list|(
name|keystoreBytes
argument_list|,
name|keystoreBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|CodecUtil
operator|.
name|writeFooter
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
name|Path
name|keystoreFile
init|=
name|keystorePath
argument_list|(
name|configDir
argument_list|)
decl_stmt|;
name|Files
operator|.
name|move
argument_list|(
name|configDir
operator|.
name|resolve
argument_list|(
name|tmpFile
argument_list|)
argument_list|,
name|keystoreFile
argument_list|,
name|StandardCopyOption
operator|.
name|REPLACE_EXISTING
argument_list|,
name|StandardCopyOption
operator|.
name|ATOMIC_MOVE
argument_list|)
expr_stmt|;
name|PosixFileAttributeView
name|attrs
init|=
name|Files
operator|.
name|getFileAttributeView
argument_list|(
name|keystoreFile
argument_list|,
name|PosixFileAttributeView
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|attrs
operator|!=
literal|null
condition|)
block|{
comment|// don't rely on umask: ensure the keystore has minimal permissions
name|attrs
operator|.
name|setPermissions
argument_list|(
name|PosixFilePermissions
operator|.
name|fromString
argument_list|(
literal|"rw-------"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Returns the names of all settings in this keystore. */
DECL|method|getSettings
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getSettings
parameter_list|()
block|{
return|return
name|settingNames
return|;
block|}
comment|// TODO: make settings accessible only to code that registered the setting
comment|/** Retrieve a string setting. The {@link SecureString} should be closed once it is used. */
DECL|method|getStringSetting
name|SecureString
name|getStringSetting
parameter_list|(
name|String
name|setting
parameter_list|)
throws|throws
name|GeneralSecurityException
block|{
name|KeyStore
operator|.
name|Entry
name|entry
init|=
name|keystore
operator|.
name|get
argument_list|()
operator|.
name|getEntry
argument_list|(
name|setting
argument_list|,
name|keystorePassword
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|instanceof
name|KeyStore
operator|.
name|SecretKeyEntry
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Secret setting "
operator|+
name|setting
operator|+
literal|" is not a string"
argument_list|)
throw|;
block|}
comment|// TODO: only allow getting a setting once?
name|KeyStore
operator|.
name|SecretKeyEntry
name|secretKeyEntry
init|=
operator|(
name|KeyStore
operator|.
name|SecretKeyEntry
operator|)
name|entry
decl_stmt|;
name|PBEKeySpec
name|keySpec
init|=
operator|(
name|PBEKeySpec
operator|)
name|secretFactory
operator|.
name|getKeySpec
argument_list|(
name|secretKeyEntry
operator|.
name|getSecretKey
argument_list|()
argument_list|,
name|PBEKeySpec
operator|.
name|class
argument_list|)
decl_stmt|;
name|SecureString
name|value
init|=
operator|new
name|SecureString
argument_list|(
name|keySpec
operator|.
name|getPassword
argument_list|()
argument_list|)
decl_stmt|;
name|keySpec
operator|.
name|clearPassword
argument_list|()
expr_stmt|;
return|return
name|value
return|;
block|}
comment|/**      * Set a string setting.      *      * @throws IllegalArgumentException if the value is not ASCII      */
DECL|method|setStringSetting
name|void
name|setStringSetting
parameter_list|(
name|String
name|setting
parameter_list|,
name|char
index|[]
name|value
parameter_list|)
throws|throws
name|GeneralSecurityException
block|{
if|if
condition|(
name|ASCII_ENCODER
operator|.
name|canEncode
argument_list|(
name|CharBuffer
operator|.
name|wrap
argument_list|(
name|value
argument_list|)
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Value must be ascii"
argument_list|)
throw|;
block|}
name|SecretKey
name|secretKey
init|=
name|secretFactory
operator|.
name|generateSecret
argument_list|(
operator|new
name|PBEKeySpec
argument_list|(
name|value
argument_list|)
argument_list|)
decl_stmt|;
name|keystore
operator|.
name|get
argument_list|()
operator|.
name|setEntry
argument_list|(
name|setting
argument_list|,
operator|new
name|KeyStore
operator|.
name|SecretKeyEntry
argument_list|(
name|secretKey
argument_list|)
argument_list|,
name|keystorePassword
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|settingNames
operator|.
name|add
argument_list|(
name|setting
argument_list|)
expr_stmt|;
block|}
comment|/** Remove the given setting from the keystore. */
DECL|method|remove
name|void
name|remove
parameter_list|(
name|String
name|setting
parameter_list|)
throws|throws
name|KeyStoreException
block|{
name|keystore
operator|.
name|get
argument_list|()
operator|.
name|deleteEntry
argument_list|(
name|setting
argument_list|)
expr_stmt|;
name|settingNames
operator|.
name|remove
argument_list|(
name|setting
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
if|if
condition|(
name|keystorePassword
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|keystorePassword
operator|.
name|get
argument_list|()
operator|.
name|destroy
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|DestroyFailedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

