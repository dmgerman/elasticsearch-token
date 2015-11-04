begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.serialization
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|serialization
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|AbstractDiffable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|Diff
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|DiffableUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|DiffableUtils
operator|.
name|KeyedReader
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
name|collect
operator|.
name|ImmutableOpenMap
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamOutput
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|io
operator|.
name|stream
operator|.
name|StreamableReader
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
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableMap
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_class
DECL|class|DiffableTests
specifier|public
class|class
name|DiffableTests
extends|extends
name|ESTestCase
block|{
DECL|method|testJdkMapDiff
specifier|public
name|void
name|testJdkMapDiff
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|TestDiffable
argument_list|>
name|before
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|before
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|TestDiffable
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|before
operator|.
name|put
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|TestDiffable
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|before
operator|.
name|put
argument_list|(
literal|"baz"
argument_list|,
operator|new
name|TestDiffable
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|before
operator|=
name|unmodifiableMap
argument_list|(
name|before
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|TestDiffable
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|putAll
argument_list|(
name|before
argument_list|)
expr_stmt|;
name|map
operator|.
name|remove
argument_list|(
literal|"bar"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"baz"
argument_list|,
operator|new
name|TestDiffable
argument_list|(
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"new"
argument_list|,
operator|new
name|TestDiffable
argument_list|(
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|TestDiffable
argument_list|>
name|after
init|=
name|unmodifiableMap
argument_list|(
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|map
argument_list|)
argument_list|)
decl_stmt|;
name|Diff
name|diff
init|=
name|DiffableUtils
operator|.
name|diff
argument_list|(
name|before
argument_list|,
name|after
argument_list|)
decl_stmt|;
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|diff
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|StreamInput
name|in
init|=
name|StreamInput
operator|.
name|wrap
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|TestDiffable
argument_list|>
name|serialized
init|=
name|DiffableUtils
operator|.
name|readJdkMapDiff
argument_list|(
name|in
argument_list|,
name|TestDiffable
operator|.
name|PROTO
argument_list|)
operator|.
name|apply
argument_list|(
name|before
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|serialized
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|serialized
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|value
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
name|serialized
operator|.
name|get
argument_list|(
literal|"baz"
argument_list|)
operator|.
name|value
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|serialized
operator|.
name|get
argument_list|(
literal|"new"
argument_list|)
operator|.
name|value
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testImmutableOpenMapDiff
specifier|public
name|void
name|testImmutableOpenMapDiff
parameter_list|()
throws|throws
name|IOException
block|{
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|TestDiffable
argument_list|>
name|builder
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|TestDiffable
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|TestDiffable
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"baz"
argument_list|,
operator|new
name|TestDiffable
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|TestDiffable
argument_list|>
name|before
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|builder
operator|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|(
name|before
argument_list|)
expr_stmt|;
name|builder
operator|.
name|remove
argument_list|(
literal|"bar"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"baz"
argument_list|,
operator|new
name|TestDiffable
argument_list|(
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"new"
argument_list|,
operator|new
name|TestDiffable
argument_list|(
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|TestDiffable
argument_list|>
name|after
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|Diff
name|diff
init|=
name|DiffableUtils
operator|.
name|diff
argument_list|(
name|before
argument_list|,
name|after
argument_list|)
decl_stmt|;
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|diff
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|StreamInput
name|in
init|=
name|StreamInput
operator|.
name|wrap
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|TestDiffable
argument_list|>
name|serialized
init|=
name|DiffableUtils
operator|.
name|readImmutableOpenMapDiff
argument_list|(
name|in
argument_list|,
operator|new
name|KeyedReader
argument_list|<
name|TestDiffable
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TestDiffable
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|String
name|key
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|TestDiffable
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Diff
argument_list|<
name|TestDiffable
argument_list|>
name|readDiffFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|String
name|key
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|AbstractDiffable
operator|.
name|readDiffFrom
argument_list|(
operator|new
name|StreamableReader
argument_list|<
name|TestDiffable
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TestDiffable
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|TestDiffable
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|,
name|in
argument_list|)
return|;
block|}
block|}
argument_list|)
operator|.
name|apply
argument_list|(
name|before
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|serialized
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|serialized
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|value
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
name|serialized
operator|.
name|get
argument_list|(
literal|"baz"
argument_list|)
operator|.
name|value
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|serialized
operator|.
name|get
argument_list|(
literal|"new"
argument_list|)
operator|.
name|value
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|TestDiffable
specifier|public
specifier|static
class|class
name|TestDiffable
extends|extends
name|AbstractDiffable
argument_list|<
name|TestDiffable
argument_list|>
block|{
DECL|field|PROTO
specifier|public
specifier|static
specifier|final
name|TestDiffable
name|PROTO
init|=
operator|new
name|TestDiffable
argument_list|(
literal|""
argument_list|)
decl_stmt|;
DECL|field|value
specifier|private
specifier|final
name|String
name|value
decl_stmt|;
DECL|method|TestDiffable
specifier|public
name|TestDiffable
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
DECL|method|value
specifier|public
name|String
name|value
parameter_list|()
block|{
return|return
name|value
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|TestDiffable
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|TestDiffable
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

