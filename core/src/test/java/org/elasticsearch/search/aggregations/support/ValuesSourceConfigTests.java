begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|support
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
name|index
operator|.
name|SortedNumericDocValues
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
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|WriteRequest
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
name|settings
operator|.
name|Settings
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
name|IndexService
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
name|engine
operator|.
name|Engine
operator|.
name|Searcher
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
name|fielddata
operator|.
name|SortedBinaryDocValues
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
name|query
operator|.
name|QueryShardContext
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
name|ESSingleNodeTestCase
import|;
end_import

begin_class
DECL|class|ValuesSourceConfigTests
specifier|public
class|class
name|ValuesSourceConfigTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testKeyword
specifier|public
name|void
name|testKeyword
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"index"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|"type"
argument_list|,
literal|"bytes"
argument_list|,
literal|"type=keyword"
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"bytes"
argument_list|,
literal|"abc"
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
init|(
name|Searcher
name|searcher
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
operator|.
name|acquireSearcher
argument_list|(
literal|"test"
argument_list|)
init|)
block|{
name|QueryShardContext
name|context
init|=
name|indexService
operator|.
name|newQueryShardContext
argument_list|(
literal|0
argument_list|,
name|searcher
operator|.
name|reader
argument_list|()
argument_list|,
parameter_list|()
lambda|->
literal|42L
argument_list|)
decl_stmt|;
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|Bytes
argument_list|>
name|config
init|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
literal|null
argument_list|,
literal|"bytes"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ValuesSource
operator|.
name|Bytes
name|valuesSource
init|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|LeafReaderContext
name|ctx
init|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|SortedBinaryDocValues
name|values
init|=
name|valuesSource
operator|.
name|bytesValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|values
operator|.
name|advanceExact
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|values
operator|.
name|docValueCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|values
operator|.
name|nextValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testEmptyKeyword
specifier|public
name|void
name|testEmptyKeyword
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"index"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|"type"
argument_list|,
literal|"bytes"
argument_list|,
literal|"type=keyword"
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|()
operator|.
name|setRefreshPolicy
argument_list|(
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
init|(
name|Searcher
name|searcher
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
operator|.
name|acquireSearcher
argument_list|(
literal|"test"
argument_list|)
init|)
block|{
name|QueryShardContext
name|context
init|=
name|indexService
operator|.
name|newQueryShardContext
argument_list|(
literal|0
argument_list|,
name|searcher
operator|.
name|reader
argument_list|()
argument_list|,
parameter_list|()
lambda|->
literal|42L
argument_list|)
decl_stmt|;
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|Bytes
argument_list|>
name|config
init|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
literal|null
argument_list|,
literal|"bytes"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ValuesSource
operator|.
name|Bytes
name|valuesSource
init|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|LeafReaderContext
name|ctx
init|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|SortedBinaryDocValues
name|values
init|=
name|valuesSource
operator|.
name|bytesValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|values
operator|.
name|advanceExact
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
literal|null
argument_list|,
literal|"bytes"
argument_list|,
literal|null
argument_list|,
literal|"abc"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|valuesSource
operator|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|values
operator|=
name|valuesSource
operator|.
name|bytesValues
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|values
operator|.
name|advanceExact
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|values
operator|.
name|docValueCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|values
operator|.
name|nextValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testUnmappedKeyword
specifier|public
name|void
name|testUnmappedKeyword
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"index"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|"type"
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|()
operator|.
name|setRefreshPolicy
argument_list|(
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
init|(
name|Searcher
name|searcher
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
operator|.
name|acquireSearcher
argument_list|(
literal|"test"
argument_list|)
init|)
block|{
name|QueryShardContext
name|context
init|=
name|indexService
operator|.
name|newQueryShardContext
argument_list|(
literal|0
argument_list|,
name|searcher
operator|.
name|reader
argument_list|()
argument_list|,
parameter_list|()
lambda|->
literal|42L
argument_list|)
decl_stmt|;
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|Bytes
argument_list|>
name|config
init|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|ValueType
operator|.
name|STRING
argument_list|,
literal|"bytes"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ValuesSource
operator|.
name|Bytes
name|valuesSource
init|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|valuesSource
argument_list|)
expr_stmt|;
name|config
operator|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|ValueType
operator|.
name|STRING
argument_list|,
literal|"bytes"
argument_list|,
literal|null
argument_list|,
literal|"abc"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|valuesSource
operator|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|LeafReaderContext
name|ctx
init|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|SortedBinaryDocValues
name|values
init|=
name|valuesSource
operator|.
name|bytesValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|values
operator|.
name|advanceExact
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|values
operator|.
name|docValueCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|values
operator|.
name|nextValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testLong
specifier|public
name|void
name|testLong
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"index"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|"type"
argument_list|,
literal|"long"
argument_list|,
literal|"type=long"
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"long"
argument_list|,
literal|42
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
init|(
name|Searcher
name|searcher
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
operator|.
name|acquireSearcher
argument_list|(
literal|"test"
argument_list|)
init|)
block|{
name|QueryShardContext
name|context
init|=
name|indexService
operator|.
name|newQueryShardContext
argument_list|(
literal|0
argument_list|,
name|searcher
operator|.
name|reader
argument_list|()
argument_list|,
parameter_list|()
lambda|->
literal|42L
argument_list|)
decl_stmt|;
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|Numeric
argument_list|>
name|config
init|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
literal|null
argument_list|,
literal|"long"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
init|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|LeafReaderContext
name|ctx
init|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|SortedNumericDocValues
name|values
init|=
name|valuesSource
operator|.
name|longValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|values
operator|.
name|advanceExact
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|values
operator|.
name|docValueCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|42
argument_list|,
name|values
operator|.
name|nextValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testEmptyLong
specifier|public
name|void
name|testEmptyLong
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"index"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|"type"
argument_list|,
literal|"long"
argument_list|,
literal|"type=long"
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|()
operator|.
name|setRefreshPolicy
argument_list|(
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
init|(
name|Searcher
name|searcher
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
operator|.
name|acquireSearcher
argument_list|(
literal|"test"
argument_list|)
init|)
block|{
name|QueryShardContext
name|context
init|=
name|indexService
operator|.
name|newQueryShardContext
argument_list|(
literal|0
argument_list|,
name|searcher
operator|.
name|reader
argument_list|()
argument_list|,
parameter_list|()
lambda|->
literal|42L
argument_list|)
decl_stmt|;
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|Numeric
argument_list|>
name|config
init|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
literal|null
argument_list|,
literal|"long"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
init|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|LeafReaderContext
name|ctx
init|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|SortedNumericDocValues
name|values
init|=
name|valuesSource
operator|.
name|longValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|values
operator|.
name|advanceExact
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
literal|null
argument_list|,
literal|"long"
argument_list|,
literal|null
argument_list|,
literal|42
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|valuesSource
operator|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|values
operator|=
name|valuesSource
operator|.
name|longValues
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|values
operator|.
name|advanceExact
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|values
operator|.
name|docValueCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|42
argument_list|,
name|values
operator|.
name|nextValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testUnmappedLong
specifier|public
name|void
name|testUnmappedLong
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"index"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|"type"
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|()
operator|.
name|setRefreshPolicy
argument_list|(
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
init|(
name|Searcher
name|searcher
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
operator|.
name|acquireSearcher
argument_list|(
literal|"test"
argument_list|)
init|)
block|{
name|QueryShardContext
name|context
init|=
name|indexService
operator|.
name|newQueryShardContext
argument_list|(
literal|0
argument_list|,
name|searcher
operator|.
name|reader
argument_list|()
argument_list|,
parameter_list|()
lambda|->
literal|42L
argument_list|)
decl_stmt|;
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|Numeric
argument_list|>
name|config
init|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|ValueType
operator|.
name|NUMBER
argument_list|,
literal|"long"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
init|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|valuesSource
argument_list|)
expr_stmt|;
name|config
operator|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|ValueType
operator|.
name|NUMBER
argument_list|,
literal|"long"
argument_list|,
literal|null
argument_list|,
literal|42
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|valuesSource
operator|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|LeafReaderContext
name|ctx
init|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|SortedNumericDocValues
name|values
init|=
name|valuesSource
operator|.
name|longValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|values
operator|.
name|advanceExact
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|values
operator|.
name|docValueCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|42
argument_list|,
name|values
operator|.
name|nextValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testBoolean
specifier|public
name|void
name|testBoolean
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"index"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|"type"
argument_list|,
literal|"bool"
argument_list|,
literal|"type=boolean"
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"bool"
argument_list|,
literal|true
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
init|(
name|Searcher
name|searcher
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
operator|.
name|acquireSearcher
argument_list|(
literal|"test"
argument_list|)
init|)
block|{
name|QueryShardContext
name|context
init|=
name|indexService
operator|.
name|newQueryShardContext
argument_list|(
literal|0
argument_list|,
name|searcher
operator|.
name|reader
argument_list|()
argument_list|,
parameter_list|()
lambda|->
literal|42L
argument_list|)
decl_stmt|;
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|Numeric
argument_list|>
name|config
init|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
literal|null
argument_list|,
literal|"bool"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
init|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|LeafReaderContext
name|ctx
init|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|SortedNumericDocValues
name|values
init|=
name|valuesSource
operator|.
name|longValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|values
operator|.
name|advanceExact
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|values
operator|.
name|docValueCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|values
operator|.
name|nextValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testEmptyBoolean
specifier|public
name|void
name|testEmptyBoolean
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"index"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|"type"
argument_list|,
literal|"bool"
argument_list|,
literal|"type=boolean"
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|()
operator|.
name|setRefreshPolicy
argument_list|(
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
init|(
name|Searcher
name|searcher
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
operator|.
name|acquireSearcher
argument_list|(
literal|"test"
argument_list|)
init|)
block|{
name|QueryShardContext
name|context
init|=
name|indexService
operator|.
name|newQueryShardContext
argument_list|(
literal|0
argument_list|,
name|searcher
operator|.
name|reader
argument_list|()
argument_list|,
parameter_list|()
lambda|->
literal|42L
argument_list|)
decl_stmt|;
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|Numeric
argument_list|>
name|config
init|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
literal|null
argument_list|,
literal|"bool"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
init|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|LeafReaderContext
name|ctx
init|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|SortedNumericDocValues
name|values
init|=
name|valuesSource
operator|.
name|longValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|values
operator|.
name|advanceExact
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|config
operator|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
literal|null
argument_list|,
literal|"bool"
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|valuesSource
operator|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|values
operator|=
name|valuesSource
operator|.
name|longValues
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|values
operator|.
name|advanceExact
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|values
operator|.
name|docValueCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|values
operator|.
name|nextValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testUnmappedBoolean
specifier|public
name|void
name|testUnmappedBoolean
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"index"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|"type"
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|()
operator|.
name|setRefreshPolicy
argument_list|(
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
init|(
name|Searcher
name|searcher
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
operator|.
name|acquireSearcher
argument_list|(
literal|"test"
argument_list|)
init|)
block|{
name|QueryShardContext
name|context
init|=
name|indexService
operator|.
name|newQueryShardContext
argument_list|(
literal|0
argument_list|,
name|searcher
operator|.
name|reader
argument_list|()
argument_list|,
parameter_list|()
lambda|->
literal|42L
argument_list|)
decl_stmt|;
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|Numeric
argument_list|>
name|config
init|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|ValueType
operator|.
name|BOOLEAN
argument_list|,
literal|"bool"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
init|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|valuesSource
argument_list|)
expr_stmt|;
name|config
operator|=
name|ValuesSourceConfig
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|ValueType
operator|.
name|BOOLEAN
argument_list|,
literal|"bool"
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|valuesSource
operator|=
name|config
operator|.
name|toValuesSource
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|LeafReaderContext
name|ctx
init|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|SortedNumericDocValues
name|values
init|=
name|valuesSource
operator|.
name|longValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|values
operator|.
name|advanceExact
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|values
operator|.
name|docValueCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|values
operator|.
name|nextValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

