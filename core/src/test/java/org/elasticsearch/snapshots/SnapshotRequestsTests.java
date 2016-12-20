begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.snapshots
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|snapshots
operator|.
name|create
operator|.
name|CreateSnapshotRequest
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
name|admin
operator|.
name|cluster
operator|.
name|snapshots
operator|.
name|restore
operator|.
name|RestoreSnapshotRequest
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
name|IndicesOptions
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
name|bytes
operator|.
name|BytesReference
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentHelper
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
import|;
end_import

begin_class
DECL|class|SnapshotRequestsTests
specifier|public
class|class
name|SnapshotRequestsTests
extends|extends
name|ESTestCase
block|{
DECL|method|testRestoreSnapshotRequestParsing
specifier|public
name|void
name|testRestoreSnapshotRequestParsing
parameter_list|()
throws|throws
name|IOException
block|{
name|RestoreSnapshotRequest
name|request
init|=
operator|new
name|RestoreSnapshotRequest
argument_list|(
literal|"test-repo"
argument_list|,
literal|"test-snap"
argument_list|)
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"indices"
argument_list|,
literal|"foo,bar,baz"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"indices"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|value
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|value
argument_list|(
literal|"bar"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|value
argument_list|(
literal|"baz"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|IndicesOptions
name|indicesOptions
init|=
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indicesOptions
operator|.
name|expandWildcardsClosed
argument_list|()
condition|)
block|{
if|if
condition|(
name|indicesOptions
operator|.
name|expandWildcardsOpen
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"expand_wildcards"
argument_list|,
literal|"all"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"expand_wildcards"
argument_list|,
literal|"closed"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|indicesOptions
operator|.
name|expandWildcardsOpen
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"expand_wildcards"
argument_list|,
literal|"open"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"expand_wildcards"
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"allow_no_indices"
argument_list|,
name|indicesOptions
operator|.
name|allowNoIndices
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"rename_pattern"
argument_list|,
literal|"rename-from"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"rename_replacement"
argument_list|,
literal|"rename-to"
argument_list|)
expr_stmt|;
name|boolean
name|partial
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"partial"
argument_list|,
name|partial
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"settings"
argument_list|)
operator|.
name|field
argument_list|(
literal|"set1"
argument_list|,
literal|"val1"
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"index_settings"
argument_list|)
operator|.
name|field
argument_list|(
literal|"set1"
argument_list|,
literal|"val2"
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"ignore_index_settings"
argument_list|,
literal|"set2,set3"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"ignore_index_settings"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|value
argument_list|(
literal|"set2"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|value
argument_list|(
literal|"set3"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|BytesReference
name|bytes
init|=
name|builder
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|request
operator|.
name|source
argument_list|(
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|bytes
argument_list|)
operator|.
name|mapOrdered
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test-repo"
argument_list|,
name|request
operator|.
name|repository
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test-snap"
argument_list|,
name|request
operator|.
name|snapshot
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|request
operator|.
name|indices
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"foo"
block|,
literal|"bar"
block|,
literal|"baz"
block|}
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"rename-from"
argument_list|,
name|request
operator|.
name|renamePattern
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"rename-to"
argument_list|,
name|request
operator|.
name|renameReplacement
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|partial
argument_list|,
name|request
operator|.
name|partial
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"val1"
argument_list|,
name|request
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
literal|"set1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|request
operator|.
name|ignoreIndexSettings
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"set2"
block|,
literal|"set3"
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testCreateSnapshotRequestParsing
specifier|public
name|void
name|testCreateSnapshotRequestParsing
parameter_list|()
throws|throws
name|IOException
block|{
name|CreateSnapshotRequest
name|request
init|=
operator|new
name|CreateSnapshotRequest
argument_list|(
literal|"test-repo"
argument_list|,
literal|"test-snap"
argument_list|)
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"indices"
argument_list|,
literal|"foo,bar,baz"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"indices"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|value
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|value
argument_list|(
literal|"bar"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|value
argument_list|(
literal|"baz"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|IndicesOptions
name|indicesOptions
init|=
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indicesOptions
operator|.
name|expandWildcardsClosed
argument_list|()
condition|)
block|{
if|if
condition|(
name|indicesOptions
operator|.
name|expandWildcardsOpen
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"expand_wildcards"
argument_list|,
literal|"all"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"expand_wildcards"
argument_list|,
literal|"closed"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|indicesOptions
operator|.
name|expandWildcardsOpen
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"expand_wildcards"
argument_list|,
literal|"open"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"expand_wildcards"
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"allow_no_indices"
argument_list|,
name|indicesOptions
operator|.
name|allowNoIndices
argument_list|()
argument_list|)
expr_stmt|;
name|boolean
name|partial
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"partial"
argument_list|,
name|partial
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"settings"
argument_list|)
operator|.
name|field
argument_list|(
literal|"set1"
argument_list|,
literal|"val1"
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"index_settings"
argument_list|)
operator|.
name|field
argument_list|(
literal|"set1"
argument_list|,
literal|"val2"
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"ignore_index_settings"
argument_list|,
literal|"set2,set3"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"ignore_index_settings"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|value
argument_list|(
literal|"set2"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|value
argument_list|(
literal|"set3"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|BytesReference
name|bytes
init|=
name|builder
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|request
operator|.
name|source
argument_list|(
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|bytes
argument_list|)
operator|.
name|mapOrdered
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test-repo"
argument_list|,
name|request
operator|.
name|repository
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test-snap"
argument_list|,
name|request
operator|.
name|snapshot
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|request
operator|.
name|indices
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"foo"
block|,
literal|"bar"
block|,
literal|"baz"
block|}
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|partial
argument_list|,
name|request
operator|.
name|partial
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"val1"
argument_list|,
name|request
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
literal|"set1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

