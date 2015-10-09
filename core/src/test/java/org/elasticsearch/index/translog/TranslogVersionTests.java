begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.translog
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|translog
package|;
end_package

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
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|VersionType
import|;
end_import

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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_comment
comment|/**  * Tests for reading old and new translog files  */
end_comment

begin_class
DECL|class|TranslogVersionTests
specifier|public
class|class
name|TranslogVersionTests
extends|extends
name|ESTestCase
block|{
DECL|method|testV0LegacyTranslogVersion
specifier|public
name|void
name|testV0LegacyTranslogVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|translogFile
init|=
name|getDataPath
argument_list|(
literal|"/org/elasticsearch/index/translog/translog-v0.binary"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"test file should exist"
argument_list|,
name|Files
operator|.
name|exists
argument_list|(
name|translogFile
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|ImmutableTranslogReader
name|reader
init|=
name|openReader
argument_list|(
name|translogFile
argument_list|,
literal|0
argument_list|)
init|)
block|{
name|assertThat
argument_list|(
literal|"a version0 stream is returned"
argument_list|,
name|reader
operator|instanceof
name|LegacyTranslogReader
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
specifier|final
name|Translog
operator|.
name|Snapshot
name|snapshot
init|=
name|reader
operator|.
name|newSnapshot
argument_list|()
init|)
block|{
specifier|final
name|Translog
operator|.
name|Operation
name|operation
init|=
name|snapshot
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"operation is the correct type correctly"
argument_list|,
name|operation
operator|.
name|opType
argument_list|()
operator|==
name|Translog
operator|.
name|Operation
operator|.
name|Type
operator|.
name|INDEX
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Translog
operator|.
name|Index
name|op
init|=
operator|(
name|Translog
operator|.
name|Index
operator|)
name|operation
decl_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"doc"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|source
argument_list|()
operator|.
name|toUtf8
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"{\"body\": \"worda wordb wordc wordd \\\"worde\\\" wordf\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|parent
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|version
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|timestamp
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1407312091791L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|ttl
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|-
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|versionType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|VersionType
operator|.
name|INTERNAL
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|snapshot
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testV1ChecksummedTranslogVersion
specifier|public
name|void
name|testV1ChecksummedTranslogVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|translogFile
init|=
name|getDataPath
argument_list|(
literal|"/org/elasticsearch/index/translog/translog-v1.binary"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"test file should exist"
argument_list|,
name|Files
operator|.
name|exists
argument_list|(
name|translogFile
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|ImmutableTranslogReader
name|reader
init|=
name|openReader
argument_list|(
name|translogFile
argument_list|,
literal|0
argument_list|)
init|)
block|{
try|try
init|(
specifier|final
name|Translog
operator|.
name|Snapshot
name|snapshot
init|=
name|reader
operator|.
name|newSnapshot
argument_list|()
init|)
block|{
name|assertThat
argument_list|(
literal|"a version1 stream is returned"
argument_list|,
name|reader
operator|instanceof
name|ImmutableTranslogReader
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Translog
operator|.
name|Operation
name|operation
init|=
name|snapshot
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"operation is the correct type correctly"
argument_list|,
name|operation
operator|.
name|opType
argument_list|()
operator|==
name|Translog
operator|.
name|Operation
operator|.
name|Type
operator|.
name|INDEX
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Translog
operator|.
name|Index
name|op
init|=
operator|(
name|Translog
operator|.
name|Index
operator|)
name|operation
decl_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Bwiq98KFSb6YjJQGeSpeiw"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"doc"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|source
argument_list|()
operator|.
name|toUtf8
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"{\"body\": \"foo\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|parent
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|version
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|timestamp
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1408627184844L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|ttl
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|-
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|op
operator|.
name|versionType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|VersionType
operator|.
name|INTERNAL
argument_list|)
argument_list|)
expr_stmt|;
comment|// There are more operations
name|int
name|opNum
init|=
literal|1
decl_stmt|;
while|while
condition|(
name|snapshot
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|opNum
operator|++
expr_stmt|;
block|}
name|assertThat
argument_list|(
literal|"there should be 5 translog operations"
argument_list|,
name|opNum
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testCorruptedTranslogs
specifier|public
name|void
name|testCorruptedTranslogs
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|Path
name|translogFile
init|=
name|getDataPath
argument_list|(
literal|"/org/elasticsearch/index/translog/translog-v1-corrupted-magic.binary"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"test file should exist"
argument_list|,
name|Files
operator|.
name|exists
argument_list|(
name|translogFile
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|openReader
argument_list|(
name|translogFile
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have thrown an exception about the header being corrupt"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TranslogCorruptedException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"translog corruption from header: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"translog looks like version 1 or later, but has corrupted header"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Path
name|translogFile
init|=
name|getDataPath
argument_list|(
literal|"/org/elasticsearch/index/translog/translog-invalid-first-byte.binary"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"test file should exist"
argument_list|,
name|Files
operator|.
name|exists
argument_list|(
name|translogFile
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|openReader
argument_list|(
name|translogFile
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have thrown an exception about the header being corrupt"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TranslogCorruptedException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"translog corruption from header: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Invalid first byte in translog file, got: 1, expected 0x00 or 0x3f"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Path
name|translogFile
init|=
name|getDataPath
argument_list|(
literal|"/org/elasticsearch/index/translog/translog-v1-corrupted-body.binary"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"test file should exist"
argument_list|,
name|Files
operator|.
name|exists
argument_list|(
name|translogFile
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|ImmutableTranslogReader
name|reader
init|=
name|openReader
argument_list|(
name|translogFile
argument_list|,
literal|0
argument_list|)
init|)
block|{
try|try
init|(
specifier|final
name|Translog
operator|.
name|Snapshot
name|snapshot
init|=
name|reader
operator|.
name|newSnapshot
argument_list|()
init|)
block|{
while|while
condition|(
name|snapshot
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{                      }
block|}
block|}
name|fail
argument_list|(
literal|"should have thrown an exception about the body being corrupted"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TranslogCorruptedException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"translog corruption from body: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"translog corruption while reading from stream"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testTruncatedTranslog
specifier|public
name|void
name|testTruncatedTranslog
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|Path
name|translogFile
init|=
name|getDataPath
argument_list|(
literal|"/org/elasticsearch/index/translog/translog-v1-truncated.binary"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"test file should exist"
argument_list|,
name|Files
operator|.
name|exists
argument_list|(
name|translogFile
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|ImmutableTranslogReader
name|reader
init|=
name|openReader
argument_list|(
name|translogFile
argument_list|,
literal|0
argument_list|)
init|)
block|{
try|try
init|(
specifier|final
name|Translog
operator|.
name|Snapshot
name|snapshot
init|=
name|reader
operator|.
name|newSnapshot
argument_list|()
init|)
block|{
while|while
condition|(
name|snapshot
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{                      }
block|}
block|}
name|fail
argument_list|(
literal|"should have thrown an exception about the body being truncated"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TranslogCorruptedException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"translog truncated: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"operation size is corrupted must be"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|openReader
specifier|public
name|ImmutableTranslogReader
name|openReader
parameter_list|(
name|Path
name|path
parameter_list|,
name|long
name|id
parameter_list|)
throws|throws
name|IOException
block|{
name|FileChannel
name|channel
init|=
name|FileChannel
operator|.
name|open
argument_list|(
name|path
argument_list|,
name|StandardOpenOption
operator|.
name|READ
argument_list|)
decl_stmt|;
try|try
block|{
specifier|final
name|ChannelReference
name|raf
init|=
operator|new
name|ChannelReference
argument_list|(
name|path
argument_list|,
name|id
argument_list|,
name|channel
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ImmutableTranslogReader
name|reader
init|=
name|ImmutableTranslogReader
operator|.
name|open
argument_list|(
name|raf
argument_list|,
operator|new
name|Checkpoint
argument_list|(
name|Files
operator|.
name|size
argument_list|(
name|path
argument_list|)
argument_list|,
name|TranslogReader
operator|.
name|UNKNOWN_OP_COUNT
argument_list|,
name|id
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|channel
operator|=
literal|null
expr_stmt|;
return|return
name|reader
return|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|close
argument_list|(
name|channel
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

