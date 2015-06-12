begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent.json
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|json
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
name|core
operator|.
name|JsonFactory
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
name|core
operator|.
name|JsonGenerator
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
name|core
operator|.
name|JsonParser
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
name|util
operator|.
name|CollectionUtils
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
name|support
operator|.
name|filtering
operator|.
name|FilteringJsonGenerator
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
DECL|class|JsonXContent
specifier|public
class|class
name|JsonXContent
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
name|jsonXContent
argument_list|)
return|;
block|}
DECL|field|jsonFactory
specifier|private
specifier|final
specifier|static
name|JsonFactory
name|jsonFactory
decl_stmt|;
DECL|field|jsonXContent
specifier|public
specifier|final
specifier|static
name|JsonXContent
name|jsonXContent
decl_stmt|;
static|static
block|{
name|jsonFactory
operator|=
operator|new
name|JsonFactory
argument_list|()
expr_stmt|;
name|jsonFactory
operator|.
name|configure
argument_list|(
name|JsonParser
operator|.
name|Feature
operator|.
name|ALLOW_UNQUOTED_FIELD_NAMES
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|jsonFactory
operator|.
name|configure
argument_list|(
name|JsonGenerator
operator|.
name|Feature
operator|.
name|QUOTE_FIELD_NAMES
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|jsonFactory
operator|.
name|configure
argument_list|(
name|JsonParser
operator|.
name|Feature
operator|.
name|ALLOW_COMMENTS
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|jsonFactory
operator|.
name|configure
argument_list|(
name|JsonFactory
operator|.
name|Feature
operator|.
name|FAIL_ON_SYMBOL_HASH_OVERFLOW
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// this trips on many mappings now...
name|jsonXContent
operator|=
operator|new
name|JsonXContent
argument_list|()
expr_stmt|;
block|}
DECL|method|JsonXContent
specifier|private
name|JsonXContent
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
name|JSON
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
literal|'\n'
return|;
block|}
DECL|method|newXContentGenerator
specifier|private
name|XContentGenerator
name|newXContentGenerator
parameter_list|(
name|JsonGenerator
name|jsonGenerator
parameter_list|)
block|{
return|return
operator|new
name|JsonXContentGenerator
argument_list|(
operator|new
name|BaseJsonGenerator
argument_list|(
name|jsonGenerator
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
name|OutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|newXContentGenerator
argument_list|(
name|jsonFactory
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
name|OutputStream
name|os
parameter_list|,
name|String
index|[]
name|filters
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|CollectionUtils
operator|.
name|isEmpty
argument_list|(
name|filters
argument_list|)
condition|)
block|{
return|return
name|createGenerator
argument_list|(
name|os
argument_list|)
return|;
block|}
name|FilteringJsonGenerator
name|jsonGenerator
init|=
operator|new
name|FilteringJsonGenerator
argument_list|(
name|jsonFactory
operator|.
name|createGenerator
argument_list|(
name|os
argument_list|,
name|JsonEncoding
operator|.
name|UTF8
argument_list|)
argument_list|,
name|filters
argument_list|)
decl_stmt|;
return|return
operator|new
name|JsonXContentGenerator
argument_list|(
name|jsonGenerator
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
name|newXContentGenerator
argument_list|(
name|jsonFactory
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
name|JsonXContentParser
argument_list|(
name|jsonFactory
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
name|JsonXContentParser
argument_list|(
name|jsonFactory
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
name|JsonXContentParser
argument_list|(
name|jsonFactory
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
name|JsonXContentParser
argument_list|(
name|jsonFactory
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
name|jsonFactory
operator|.
name|createParser
argument_list|(
name|reader
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit
