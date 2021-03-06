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
name|test
operator|.
name|ESTestCase
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
name|List
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
name|containsInAnyOrder
import|;
end_import

begin_class
DECL|class|SnapshotUtilsTests
specifier|public
class|class
name|SnapshotUtilsTests
extends|extends
name|ESTestCase
block|{
DECL|method|testIndexNameFiltering
specifier|public
name|void
name|testIndexNameFiltering
parameter_list|()
block|{
name|assertIndexNameFiltering
argument_list|(
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
argument_list|,
operator|new
name|String
index|[]
block|{}
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
name|assertIndexNameFiltering
argument_list|(
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
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"*"
block|}
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
name|assertIndexNameFiltering
argument_list|(
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
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"_all"
block|}
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
name|assertIndexNameFiltering
argument_list|(
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
name|assertIndexNameFiltering
argument_list|(
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
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"foo"
block|}
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"foo"
block|}
argument_list|)
expr_stmt|;
name|assertIndexNameFiltering
argument_list|(
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
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"baz"
block|,
literal|"not_available"
block|}
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"baz"
block|}
argument_list|)
expr_stmt|;
name|assertIndexNameFiltering
argument_list|(
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
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"ba*"
block|,
literal|"-bar"
block|,
literal|"-baz"
block|}
argument_list|,
operator|new
name|String
index|[]
block|{}
argument_list|)
expr_stmt|;
name|assertIndexNameFiltering
argument_list|(
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
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-bar"
block|}
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"foo"
block|,
literal|"baz"
block|}
argument_list|)
expr_stmt|;
name|assertIndexNameFiltering
argument_list|(
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
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-ba*"
block|}
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"foo"
block|}
argument_list|)
expr_stmt|;
name|assertIndexNameFiltering
argument_list|(
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
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"+ba*"
block|}
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"bar"
block|,
literal|"baz"
block|}
argument_list|)
expr_stmt|;
name|assertIndexNameFiltering
argument_list|(
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
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"+bar"
block|,
literal|"+foo"
block|}
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"bar"
block|,
literal|"foo"
block|}
argument_list|)
expr_stmt|;
name|assertIndexNameFiltering
argument_list|(
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
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"zzz"
block|,
literal|"bar"
block|}
argument_list|,
name|IndicesOptions
operator|.
name|lenientExpandOpen
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"bar"
block|}
argument_list|)
expr_stmt|;
name|assertIndexNameFiltering
argument_list|(
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
argument_list|,
operator|new
name|String
index|[]
block|{
literal|""
block|}
argument_list|,
name|IndicesOptions
operator|.
name|lenientExpandOpen
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{}
argument_list|)
expr_stmt|;
name|assertIndexNameFiltering
argument_list|(
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
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"foo"
block|,
literal|""
block|,
literal|"ba*"
block|}
argument_list|,
name|IndicesOptions
operator|.
name|lenientExpandOpen
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
block|}
DECL|method|assertIndexNameFiltering
specifier|private
name|void
name|assertIndexNameFiltering
parameter_list|(
name|String
index|[]
name|indices
parameter_list|,
name|String
index|[]
name|filter
parameter_list|,
name|String
index|[]
name|expected
parameter_list|)
block|{
name|assertIndexNameFiltering
argument_list|(
name|indices
argument_list|,
name|filter
argument_list|,
name|IndicesOptions
operator|.
name|lenientExpandOpen
argument_list|()
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
DECL|method|assertIndexNameFiltering
specifier|private
name|void
name|assertIndexNameFiltering
parameter_list|(
name|String
index|[]
name|indices
parameter_list|,
name|String
index|[]
name|filter
parameter_list|,
name|IndicesOptions
name|indicesOptions
parameter_list|,
name|String
index|[]
name|expected
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|indicesList
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|indices
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|actual
init|=
name|SnapshotUtils
operator|.
name|filterIndices
argument_list|(
name|indicesList
argument_list|,
name|filter
argument_list|,
name|indicesOptions
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|actual
argument_list|,
name|containsInAnyOrder
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

