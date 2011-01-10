begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.xcontent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|xcontent
package|;
end_package

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
name|FastByteArrayOutputStream
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
name|XContentFactory
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
name|XContentType
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
name|QueryBuilderException
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|BaseQueryBuilder
specifier|public
specifier|abstract
class|class
name|BaseQueryBuilder
implements|implements
name|XContentQueryBuilder
block|{
DECL|method|buildAsUnsafeBytes
annotation|@
name|Override
specifier|public
name|FastByteArrayOutputStream
name|buildAsUnsafeBytes
parameter_list|()
throws|throws
name|QueryBuilderException
block|{
return|return
name|buildAsUnsafeBytes
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
return|;
block|}
DECL|method|buildAsUnsafeBytes
annotation|@
name|Override
specifier|public
name|FastByteArrayOutputStream
name|buildAsUnsafeBytes
parameter_list|(
name|XContentType
name|contentType
parameter_list|)
throws|throws
name|QueryBuilderException
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|contentType
argument_list|)
decl_stmt|;
name|toXContent
argument_list|(
name|builder
argument_list|,
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|unsafeStream
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueryBuilderException
argument_list|(
literal|"Failed to build query"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|buildAsBytes
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|buildAsBytes
parameter_list|()
throws|throws
name|QueryBuilderException
block|{
return|return
name|buildAsBytes
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
return|;
block|}
DECL|method|buildAsBytes
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|buildAsBytes
parameter_list|(
name|XContentType
name|contentType
parameter_list|)
throws|throws
name|QueryBuilderException
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|contentType
argument_list|)
decl_stmt|;
name|toXContent
argument_list|(
name|builder
argument_list|,
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|copiedBytes
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueryBuilderException
argument_list|(
literal|"Failed to build query"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|toXContent
annotation|@
name|Override
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|doXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|doXContent
specifier|protected
specifier|abstract
name|void
name|doXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

