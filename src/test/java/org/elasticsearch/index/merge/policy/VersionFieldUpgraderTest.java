begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.merge.policy
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|merge
operator|.
name|policy
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
name|analysis
operator|.
name|CannedTokenStream
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
name|analysis
operator|.
name|Token
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
name|document
operator|.
name|Document
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
name|document
operator|.
name|NumericDocValuesField
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
name|document
operator|.
name|TextField
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
name|index
operator|.
name|CodecReader
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
name|index
operator|.
name|DirectoryReader
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
name|index
operator|.
name|DocValuesType
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
name|index
operator|.
name|FieldInfo
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
name|index
operator|.
name|IndexWriter
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
name|index
operator|.
name|IndexWriterConfig
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
name|util
operator|.
name|BytesRef
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
name|TestUtil
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
name|Numbers
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
name|mapper
operator|.
name|internal
operator|.
name|UidFieldMapper
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
name|mapper
operator|.
name|internal
operator|.
name|VersionFieldMapper
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
name|ElasticsearchLuceneTestCase
import|;
end_import

begin_comment
comment|/** Tests upgrading old document versions from _uid payloads to _version docvalues */
end_comment

begin_class
DECL|class|VersionFieldUpgraderTest
specifier|public
class|class
name|VersionFieldUpgraderTest
extends|extends
name|ElasticsearchLuceneTestCase
block|{
comment|/** Simple test: one doc in the old format, check that it looks correct */
DECL|method|testUpgradeOneDocument
specifier|public
name|void
name|testUpgradeOneDocument
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|iw
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
operator|new
name|IndexWriterConfig
argument_list|(
literal|null
argument_list|)
argument_list|)
decl_stmt|;
comment|// add a document with a _uid having a payload of 3
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|Token
name|token
init|=
operator|new
name|Token
argument_list|(
literal|"1"
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|token
operator|.
name|setPayload
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|Numbers
operator|.
name|longToBytes
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|CannedTokenStream
argument_list|(
name|token
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|iw
operator|.
name|commit
argument_list|()
expr_stmt|;
name|CodecReader
name|reader
init|=
name|getOnlySegmentReader
argument_list|(
name|DirectoryReader
operator|.
name|open
argument_list|(
name|iw
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|CodecReader
name|upgraded
init|=
name|VersionFieldUpgrader
operator|.
name|wrap
argument_list|(
name|reader
argument_list|)
decl_stmt|;
comment|// we need to be upgraded, should be a different instance
name|assertNotSame
argument_list|(
name|reader
argument_list|,
name|upgraded
argument_list|)
expr_stmt|;
comment|// make sure we can see our numericdocvalues in fieldinfos
name|FieldInfo
name|versionField
init|=
name|upgraded
operator|.
name|getFieldInfos
argument_list|()
operator|.
name|fieldInfo
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|versionField
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DocValuesType
operator|.
name|NUMERIC
argument_list|,
name|versionField
operator|.
name|getDocValuesType
argument_list|()
argument_list|)
expr_stmt|;
comment|// should have a value of 3, and be visible in docsWithField
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|upgraded
operator|.
name|getNumericDocValues
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|upgraded
operator|.
name|getDocsWithField
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify filterreader with checkindex
name|TestUtil
operator|.
name|checkReader
argument_list|(
name|upgraded
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|iw
operator|.
name|close
argument_list|()
expr_stmt|;
name|dir
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/** test that we are a non-op if the segment already has the version field */
DECL|method|testAlreadyUpgraded
specifier|public
name|void
name|testAlreadyUpgraded
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|iw
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
operator|new
name|IndexWriterConfig
argument_list|(
literal|null
argument_list|)
argument_list|)
decl_stmt|;
comment|// add a document with a _uid having a payload of 3
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|Token
name|token
init|=
operator|new
name|Token
argument_list|(
literal|"1"
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|token
operator|.
name|setPayload
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|Numbers
operator|.
name|longToBytes
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|CannedTokenStream
argument_list|(
name|token
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|,
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|iw
operator|.
name|commit
argument_list|()
expr_stmt|;
name|CodecReader
name|reader
init|=
name|getOnlySegmentReader
argument_list|(
name|DirectoryReader
operator|.
name|open
argument_list|(
name|iw
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|CodecReader
name|upgraded
init|=
name|VersionFieldUpgrader
operator|.
name|wrap
argument_list|(
name|reader
argument_list|)
decl_stmt|;
comment|// we already upgraded: should be same instance
name|assertSame
argument_list|(
name|reader
argument_list|,
name|upgraded
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|iw
operator|.
name|close
argument_list|()
expr_stmt|;
name|dir
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/** Test upgrading two documents */
DECL|method|testUpgradeTwoDocuments
specifier|public
name|void
name|testUpgradeTwoDocuments
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriter
name|iw
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
operator|new
name|IndexWriterConfig
argument_list|(
literal|null
argument_list|)
argument_list|)
decl_stmt|;
comment|// add a document with a _uid having a payload of 3
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|Token
name|token
init|=
operator|new
name|Token
argument_list|(
literal|"1"
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|token
operator|.
name|setPayload
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|Numbers
operator|.
name|longToBytes
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|CannedTokenStream
argument_list|(
name|token
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|doc
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|token
operator|=
operator|new
name|Token
argument_list|(
literal|"2"
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|token
operator|.
name|setPayload
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|Numbers
operator|.
name|longToBytes
argument_list|(
literal|4
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|CannedTokenStream
argument_list|(
name|token
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|iw
operator|.
name|commit
argument_list|()
expr_stmt|;
name|CodecReader
name|reader
init|=
name|getOnlySegmentReader
argument_list|(
name|DirectoryReader
operator|.
name|open
argument_list|(
name|iw
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|CodecReader
name|upgraded
init|=
name|VersionFieldUpgrader
operator|.
name|wrap
argument_list|(
name|reader
argument_list|)
decl_stmt|;
comment|// we need to be upgraded, should be a different instance
name|assertNotSame
argument_list|(
name|reader
argument_list|,
name|upgraded
argument_list|)
expr_stmt|;
comment|// make sure we can see our numericdocvalues in fieldinfos
name|FieldInfo
name|versionField
init|=
name|upgraded
operator|.
name|getFieldInfos
argument_list|()
operator|.
name|fieldInfo
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|versionField
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DocValuesType
operator|.
name|NUMERIC
argument_list|,
name|versionField
operator|.
name|getDocValuesType
argument_list|()
argument_list|)
expr_stmt|;
comment|// should have a values of 3 and 4, and be visible in docsWithField
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|upgraded
operator|.
name|getNumericDocValues
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|upgraded
operator|.
name|getNumericDocValues
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|upgraded
operator|.
name|getDocsWithField
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|upgraded
operator|.
name|getDocsWithField
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify filterreader with checkindex
name|TestUtil
operator|.
name|checkReader
argument_list|(
name|upgraded
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|iw
operator|.
name|close
argument_list|()
expr_stmt|;
name|dir
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

