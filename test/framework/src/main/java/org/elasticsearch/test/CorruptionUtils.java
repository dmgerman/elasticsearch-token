begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomPicks
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
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
name|Directory
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
name|FSDirectory
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
name|elasticsearch
operator|.
name|common
operator|.
name|logging
operator|.
name|ESLoggerFactory
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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|FileChannel
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
name|StandardOpenOption
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|LuceneTestCase
operator|.
name|assumeTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|notNullValue
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
name|assertThat
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
name|assertTrue
import|;
end_import

begin_class
DECL|class|CorruptionUtils
specifier|public
specifier|final
class|class
name|CorruptionUtils
block|{
DECL|field|logger
specifier|private
specifier|static
name|Logger
name|logger
init|=
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
DECL|method|CorruptionUtils
specifier|private
name|CorruptionUtils
parameter_list|()
block|{}
comment|/**      * Corrupts a random file at a random position      */
DECL|method|corruptFile
specifier|public
specifier|static
name|void
name|corruptFile
parameter_list|(
name|Random
name|random
parameter_list|,
name|Path
modifier|...
name|files
parameter_list|)
throws|throws
name|IOException
block|{
name|assertTrue
argument_list|(
literal|"files must be non-empty"
argument_list|,
name|files
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
specifier|final
name|Path
name|fileToCorrupt
init|=
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
name|random
argument_list|,
name|files
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fileToCorrupt
operator|+
literal|" is not a file"
argument_list|,
name|Files
operator|.
name|isRegularFile
argument_list|(
name|fileToCorrupt
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|Directory
name|dir
init|=
name|FSDirectory
operator|.
name|open
argument_list|(
name|fileToCorrupt
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|getParent
argument_list|()
argument_list|)
init|)
block|{
name|long
name|checksumBeforeCorruption
decl_stmt|;
try|try
init|(
name|IndexInput
name|input
init|=
name|dir
operator|.
name|openInput
argument_list|(
name|fileToCorrupt
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|IOContext
operator|.
name|DEFAULT
argument_list|)
init|)
block|{
name|checksumBeforeCorruption
operator|=
name|CodecUtil
operator|.
name|retrieveChecksum
argument_list|(
name|input
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|FileChannel
name|raf
init|=
name|FileChannel
operator|.
name|open
argument_list|(
name|fileToCorrupt
argument_list|,
name|StandardOpenOption
operator|.
name|READ
argument_list|,
name|StandardOpenOption
operator|.
name|WRITE
argument_list|)
init|)
block|{
comment|// read
name|raf
operator|.
name|position
argument_list|(
name|random
operator|.
name|nextInt
argument_list|(
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|raf
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|filePointer
init|=
name|raf
operator|.
name|position
argument_list|()
decl_stmt|;
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|raf
operator|.
name|read
argument_list|(
name|bb
argument_list|)
expr_stmt|;
name|bb
operator|.
name|flip
argument_list|()
expr_stmt|;
comment|// corrupt
name|byte
name|oldValue
init|=
name|bb
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|byte
name|newValue
init|=
call|(
name|byte
call|)
argument_list|(
name|oldValue
operator|+
literal|1
argument_list|)
decl_stmt|;
name|bb
operator|.
name|put
argument_list|(
literal|0
argument_list|,
name|newValue
argument_list|)
expr_stmt|;
comment|// rewrite
name|raf
operator|.
name|position
argument_list|(
name|filePointer
argument_list|)
expr_stmt|;
name|raf
operator|.
name|write
argument_list|(
name|bb
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Corrupting file --  flipping at position {} from {} to {} file: {}"
argument_list|,
name|filePointer
argument_list|,
name|Integer
operator|.
name|toHexString
argument_list|(
name|oldValue
argument_list|)
argument_list|,
name|Integer
operator|.
name|toHexString
argument_list|(
name|newValue
argument_list|)
argument_list|,
name|fileToCorrupt
operator|.
name|getFileName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|long
name|checksumAfterCorruption
decl_stmt|;
name|long
name|actualChecksumAfterCorruption
decl_stmt|;
try|try
init|(
name|ChecksumIndexInput
name|input
init|=
name|dir
operator|.
name|openChecksumInput
argument_list|(
name|fileToCorrupt
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|IOContext
operator|.
name|DEFAULT
argument_list|)
init|)
block|{
name|assertThat
argument_list|(
name|input
operator|.
name|getFilePointer
argument_list|()
argument_list|,
name|is
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|input
operator|.
name|seek
argument_list|(
name|input
operator|.
name|length
argument_list|()
operator|-
literal|8
argument_list|)
expr_stmt|;
comment|// one long is the checksum... 8 bytes
name|checksumAfterCorruption
operator|=
name|input
operator|.
name|getChecksum
argument_list|()
expr_stmt|;
name|actualChecksumAfterCorruption
operator|=
name|input
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
comment|// we need to add assumptions here that the checksums actually really don't match there is a small chance to get collisions
comment|// in the checksum which is ok though....
name|StringBuilder
name|msg
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"before: ["
argument_list|)
operator|.
name|append
argument_list|(
name|checksumBeforeCorruption
argument_list|)
operator|.
name|append
argument_list|(
literal|"] "
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"after: ["
argument_list|)
operator|.
name|append
argument_list|(
name|checksumAfterCorruption
argument_list|)
operator|.
name|append
argument_list|(
literal|"] "
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"checksum value after corruption: "
argument_list|)
operator|.
name|append
argument_list|(
name|actualChecksumAfterCorruption
argument_list|)
operator|.
name|append
argument_list|(
literal|"] "
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"file: "
argument_list|)
operator|.
name|append
argument_list|(
name|fileToCorrupt
operator|.
name|getFileName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" length: "
argument_list|)
operator|.
name|append
argument_list|(
name|dir
operator|.
name|fileLength
argument_list|(
name|fileToCorrupt
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Checksum {}"
argument_list|,
name|msg
argument_list|)
expr_stmt|;
name|assumeTrue
argument_list|(
literal|"Checksum collision - "
operator|+
name|msg
operator|.
name|toString
argument_list|()
argument_list|,
name|checksumAfterCorruption
operator|!=
name|checksumBeforeCorruption
comment|// collision
operator|||
name|actualChecksumAfterCorruption
operator|!=
name|checksumBeforeCorruption
argument_list|)
expr_stmt|;
comment|// checksum corrupted
name|assertThat
argument_list|(
literal|"no file corrupted"
argument_list|,
name|fileToCorrupt
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

