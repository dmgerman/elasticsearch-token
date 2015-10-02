begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.hash
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|hash
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigInteger
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
name|security
operator|.
name|MessageDigest
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_class
DECL|class|MessageDigestsTests
specifier|public
class|class
name|MessageDigestsTests
extends|extends
name|ESTestCase
block|{
DECL|method|assertHash
specifier|private
name|void
name|assertHash
parameter_list|(
name|String
name|expected
parameter_list|,
name|String
name|test
parameter_list|,
name|MessageDigest
name|messageDigest
parameter_list|)
block|{
name|String
name|actual
init|=
name|MessageDigests
operator|.
name|toHexString
argument_list|(
name|messageDigest
operator|.
name|digest
argument_list|(
name|test
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMd5
specifier|public
name|void
name|testMd5
parameter_list|()
throws|throws
name|Exception
block|{
name|assertHash
argument_list|(
literal|"d41d8cd98f00b204e9800998ecf8427e"
argument_list|,
literal|""
argument_list|,
name|MessageDigests
operator|.
name|md5
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"900150983cd24fb0d6963f7d28e17f72"
argument_list|,
literal|"abc"
argument_list|,
name|MessageDigests
operator|.
name|md5
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"8215ef0796a20bcaaae116d3876c664a"
argument_list|,
literal|"abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
argument_list|,
name|MessageDigests
operator|.
name|md5
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"7707d6ae4e027c70eea2a935c2296f21"
argument_list|,
operator|new
name|String
argument_list|(
operator|new
name|char
index|[
literal|1000000
index|]
argument_list|)
operator|.
name|replace
argument_list|(
literal|"\0"
argument_list|,
literal|"a"
argument_list|)
argument_list|,
name|MessageDigests
operator|.
name|md5
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"9e107d9d372bb6826bd81d3542a419d6"
argument_list|,
literal|"The quick brown fox jumps over the lazy dog"
argument_list|,
name|MessageDigests
operator|.
name|md5
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"1055d3e698d289f2af8663725127bd4b"
argument_list|,
literal|"The quick brown fox jumps over the lazy cog"
argument_list|,
name|MessageDigests
operator|.
name|md5
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSha1
specifier|public
name|void
name|testSha1
parameter_list|()
throws|throws
name|Exception
block|{
name|assertHash
argument_list|(
literal|"da39a3ee5e6b4b0d3255bfef95601890afd80709"
argument_list|,
literal|""
argument_list|,
name|MessageDigests
operator|.
name|sha1
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"a9993e364706816aba3e25717850c26c9cd0d89d"
argument_list|,
literal|"abc"
argument_list|,
name|MessageDigests
operator|.
name|sha1
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"84983e441c3bd26ebaae4aa1f95129e5e54670f1"
argument_list|,
literal|"abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
argument_list|,
name|MessageDigests
operator|.
name|sha1
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"34aa973cd4c4daa4f61eeb2bdbad27316534016f"
argument_list|,
operator|new
name|String
argument_list|(
operator|new
name|char
index|[
literal|1000000
index|]
argument_list|)
operator|.
name|replace
argument_list|(
literal|"\0"
argument_list|,
literal|"a"
argument_list|)
argument_list|,
name|MessageDigests
operator|.
name|sha1
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"2fd4e1c67a2d28fced849ee1bb76e7391b93eb12"
argument_list|,
literal|"The quick brown fox jumps over the lazy dog"
argument_list|,
name|MessageDigests
operator|.
name|sha1
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3"
argument_list|,
literal|"The quick brown fox jumps over the lazy cog"
argument_list|,
name|MessageDigests
operator|.
name|sha1
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSha256
specifier|public
name|void
name|testSha256
parameter_list|()
throws|throws
name|Exception
block|{
name|assertHash
argument_list|(
literal|"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
argument_list|,
literal|""
argument_list|,
name|MessageDigests
operator|.
name|sha256
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
argument_list|,
literal|"abc"
argument_list|,
name|MessageDigests
operator|.
name|sha256
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1"
argument_list|,
literal|"abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
argument_list|,
name|MessageDigests
operator|.
name|sha256
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"cdc76e5c9914fb9281a1c7e284d73e67f1809a48a497200e046d39ccc7112cd0"
argument_list|,
operator|new
name|String
argument_list|(
operator|new
name|char
index|[
literal|1000000
index|]
argument_list|)
operator|.
name|replace
argument_list|(
literal|"\0"
argument_list|,
literal|"a"
argument_list|)
argument_list|,
name|MessageDigests
operator|.
name|sha256
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592"
argument_list|,
literal|"The quick brown fox jumps over the lazy dog"
argument_list|,
name|MessageDigests
operator|.
name|sha256
argument_list|()
argument_list|)
expr_stmt|;
name|assertHash
argument_list|(
literal|"e4c4d8f3bf76b692de791a173e05321150f7a345b46484fe427f6acc7ecc81be"
argument_list|,
literal|"The quick brown fox jumps over the lazy cog"
argument_list|,
name|MessageDigests
operator|.
name|sha256
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testToHexString
specifier|public
name|void
name|testToHexString
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1024
condition|;
name|i
operator|++
control|)
block|{
name|BigInteger
name|expected
init|=
name|BigInteger
operator|.
name|probablePrime
argument_list|(
literal|256
argument_list|,
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|expected
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|String
name|hex
init|=
name|MessageDigests
operator|.
name|toHexString
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|String
name|zeros
init|=
operator|new
name|String
argument_list|(
operator|new
name|char
index|[
name|bytes
operator|.
name|length
operator|*
literal|2
index|]
argument_list|)
operator|.
name|replace
argument_list|(
literal|"\0"
argument_list|,
literal|"0"
argument_list|)
decl_stmt|;
name|String
name|expectedAsString
init|=
name|expected
operator|.
name|toString
argument_list|(
literal|16
argument_list|)
decl_stmt|;
name|String
name|expectedHex
init|=
name|zeros
operator|.
name|substring
argument_list|(
name|expectedAsString
operator|.
name|length
argument_list|()
argument_list|)
operator|+
name|expectedAsString
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedHex
argument_list|,
name|hex
argument_list|)
expr_stmt|;
name|BigInteger
name|actual
init|=
operator|new
name|BigInteger
argument_list|(
name|hex
argument_list|,
literal|16
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

