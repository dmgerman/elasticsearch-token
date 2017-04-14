begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.uid
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|uid
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
name|Field
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
name|index
operator|.
name|LeafReaderContext
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
name|Bits
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
name|FixedBitSet
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
name|lucene
operator|.
name|Lucene
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
name|lucene
operator|.
name|uid
operator|.
name|VersionsAndSeqNoResolver
operator|.
name|DocIdAndVersion
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
name|ESTestCase
import|;
end_import

begin_comment
comment|/**  * test per-segment lookup of version-related data structures  */
end_comment

begin_class
DECL|class|VersionLookupTests
specifier|public
class|class
name|VersionLookupTests
extends|extends
name|ESTestCase
block|{
comment|/**      * test version lookup actually works      */
DECL|method|testSimple
specifier|public
name|void
name|testSimple
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
name|writer
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
operator|new
name|IndexWriterConfig
argument_list|(
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
argument_list|)
decl_stmt|;
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"6"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
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
literal|87
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|DirectoryReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|)
decl_stmt|;
name|LeafReaderContext
name|segment
init|=
name|reader
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|PerThreadIDVersionAndSeqNoLookup
name|lookup
init|=
operator|new
name|PerThreadIDVersionAndSeqNoLookup
argument_list|(
name|segment
operator|.
name|reader
argument_list|()
argument_list|)
decl_stmt|;
comment|// found doc
name|DocIdAndVersion
name|result
init|=
name|lookup
operator|.
name|lookupVersion
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"6"
argument_list|)
argument_list|,
literal|null
argument_list|,
name|segment
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|87
argument_list|,
name|result
operator|.
name|version
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|result
operator|.
name|docId
argument_list|)
expr_stmt|;
comment|// not found doc
name|assertNull
argument_list|(
name|lookup
operator|.
name|lookupVersion
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"7"
argument_list|)
argument_list|,
literal|null
argument_list|,
name|segment
argument_list|)
argument_list|)
expr_stmt|;
comment|// deleted doc
name|assertNull
argument_list|(
name|lookup
operator|.
name|lookupVersion
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"6"
argument_list|)
argument_list|,
operator|new
name|Bits
operator|.
name|MatchNoBits
argument_list|(
literal|1
argument_list|)
argument_list|,
name|segment
argument_list|)
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|writer
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
comment|/**      * test version lookup with two documents matching the ID      */
DECL|method|testTwoDocuments
specifier|public
name|void
name|testTwoDocuments
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
name|writer
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
operator|new
name|IndexWriterConfig
argument_list|(
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
argument_list|)
decl_stmt|;
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
literal|"6"
argument_list|,
name|UidFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
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
literal|87
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|DirectoryReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|)
decl_stmt|;
name|LeafReaderContext
name|segment
init|=
name|reader
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|PerThreadIDVersionAndSeqNoLookup
name|lookup
init|=
operator|new
name|PerThreadIDVersionAndSeqNoLookup
argument_list|(
name|segment
operator|.
name|reader
argument_list|()
argument_list|)
decl_stmt|;
comment|// return the last doc when there are duplicates
name|DocIdAndVersion
name|result
init|=
name|lookup
operator|.
name|lookupVersion
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"6"
argument_list|)
argument_list|,
literal|null
argument_list|,
name|segment
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|87
argument_list|,
name|result
operator|.
name|version
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|docId
argument_list|)
expr_stmt|;
comment|// delete the first doc only
name|FixedBitSet
name|live
init|=
operator|new
name|FixedBitSet
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|live
operator|.
name|set
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|result
operator|=
name|lookup
operator|.
name|lookupVersion
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"6"
argument_list|)
argument_list|,
name|live
argument_list|,
name|segment
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|87
argument_list|,
name|result
operator|.
name|version
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|docId
argument_list|)
expr_stmt|;
comment|// delete the second doc only
name|live
operator|.
name|clear
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|live
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|result
operator|=
name|lookup
operator|.
name|lookupVersion
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"6"
argument_list|)
argument_list|,
name|live
argument_list|,
name|segment
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|87
argument_list|,
name|result
operator|.
name|version
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|result
operator|.
name|docId
argument_list|)
expr_stmt|;
comment|// delete both docs
name|assertNull
argument_list|(
name|lookup
operator|.
name|lookupVersion
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"6"
argument_list|)
argument_list|,
operator|new
name|Bits
operator|.
name|MatchNoBits
argument_list|(
literal|2
argument_list|)
argument_list|,
name|segment
argument_list|)
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|writer
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

