begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent.smile
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|smile
package|;
end_package

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|JsonEncoding
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|dataformat
operator|.
name|smile
operator|.
name|SmileFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|dataformat
operator|.
name|smile
operator|.
name|SmileGenerator
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
name|io
operator|.
name|FastStringReader
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
name|*
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
name|json
operator|.
name|JsonXContentParser
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * A JSON based content implementation using Jackson.  */
end_comment

begin_class
DECL|class|SmileXContent
specifier|public
class|class
name|SmileXContent
implements|implements
name|XContent
block|{
DECL|method|contentBuilder
specifier|public
specifier|static
name|XContentBuilder
name|contentBuilder
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|XContentBuilder
operator|.
name|builder
argument_list|(
name|smileXContent
argument_list|)
return|;
block|}
DECL|field|smileFactory
specifier|final
specifier|static
name|SmileFactory
name|smileFactory
decl_stmt|;
DECL|field|smileXContent
specifier|public
specifier|final
specifier|static
name|SmileXContent
name|smileXContent
decl_stmt|;
static|static
block|{
name|smileFactory
operator|=
operator|new
name|SmileFactory
argument_list|()
expr_stmt|;
name|smileFactory
operator|.
name|configure
argument_list|(
name|SmileGenerator
operator|.
name|Feature
operator|.
name|ENCODE_BINARY_AS_7BIT
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// for now, this is an overhead, might make sense for web sockets
name|smileXContent
operator|=
operator|new
name|SmileXContent
argument_list|()
expr_stmt|;
block|}
DECL|method|SmileXContent
specifier|private
name|SmileXContent
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|type
specifier|public
name|XContentType
name|type
parameter_list|()
block|{
return|return
name|XContentType
operator|.
name|SMILE
return|;
block|}
annotation|@
name|Override
DECL|method|streamSeparator
specifier|public
name|byte
name|streamSeparator
parameter_list|()
block|{
return|return
operator|(
name|byte
operator|)
literal|0xFF
return|;
block|}
annotation|@
name|Override
DECL|method|createGenerator
specifier|public
name|XContentGenerator
name|createGenerator
parameter_list|(
name|OutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|SmileXContentGenerator
argument_list|(
name|smileFactory
operator|.
name|createGenerator
argument_list|(
name|os
argument_list|,
name|JsonEncoding
operator|.
name|UTF8
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createGenerator
specifier|public
name|XContentGenerator
name|createGenerator
parameter_list|(
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|SmileXContentGenerator
argument_list|(
name|smileFactory
operator|.
name|createGenerator
argument_list|(
name|writer
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createParser
specifier|public
name|XContentParser
name|createParser
parameter_list|(
name|String
name|content
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|SmileXContentParser
argument_list|(
name|smileFactory
operator|.
name|createParser
argument_list|(
operator|new
name|FastStringReader
argument_list|(
name|content
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createParser
specifier|public
name|XContentParser
name|createParser
parameter_list|(
name|InputStream
name|is
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|SmileXContentParser
argument_list|(
name|smileFactory
operator|.
name|createParser
argument_list|(
name|is
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createParser
specifier|public
name|XContentParser
name|createParser
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|SmileXContentParser
argument_list|(
name|smileFactory
operator|.
name|createParser
argument_list|(
name|data
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createParser
specifier|public
name|XContentParser
name|createParser
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|SmileXContentParser
argument_list|(
name|smileFactory
operator|.
name|createParser
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createParser
specifier|public
name|XContentParser
name|createParser
parameter_list|(
name|BytesReference
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bytes
operator|.
name|hasArray
argument_list|()
condition|)
block|{
return|return
name|createParser
argument_list|(
name|bytes
operator|.
name|array
argument_list|()
argument_list|,
name|bytes
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|bytes
operator|.
name|length
argument_list|()
argument_list|)
return|;
block|}
return|return
name|createParser
argument_list|(
name|bytes
operator|.
name|streamInput
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createParser
specifier|public
name|XContentParser
name|createParser
parameter_list|(
name|Reader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|JsonXContentParser
argument_list|(
name|smileFactory
operator|.
name|createJsonParser
argument_list|(
name|reader
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

