begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent.cbor
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|cbor
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
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|dataformat
operator|.
name|cbor
operator|.
name|CBORFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|NamedXContentRegistry
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
name|XContent
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
name|XContentGenerator
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
name|XContentParser
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Reader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * A CBOR based content implementation using Jackson.  */
end_comment

begin_class
DECL|class|CborXContent
specifier|public
class|class
name|CborXContent
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
name|cborXContent
argument_list|)
return|;
block|}
DECL|field|cborFactory
specifier|static
specifier|final
name|CBORFactory
name|cborFactory
decl_stmt|;
DECL|field|cborXContent
specifier|public
specifier|static
specifier|final
name|CborXContent
name|cborXContent
decl_stmt|;
static|static
block|{
name|cborFactory
operator|=
operator|new
name|CBORFactory
argument_list|()
expr_stmt|;
name|cborFactory
operator|.
name|configure
argument_list|(
name|CBORFactory
operator|.
name|Feature
operator|.
name|FAIL_ON_SYMBOL_HASH_OVERFLOW
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// this trips on many mappings now...
comment|// Do not automatically close unclosed objects/arrays in com.fasterxml.jackson.dataformat.cbor.CBORGenerator#close() method
name|cborFactory
operator|.
name|configure
argument_list|(
name|JsonGenerator
operator|.
name|Feature
operator|.
name|AUTO_CLOSE_JSON_CONTENT
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|cborFactory
operator|.
name|configure
argument_list|(
name|JsonParser
operator|.
name|Feature
operator|.
name|STRICT_DUPLICATE_DETECTION
argument_list|,
name|XContent
operator|.
name|isStrictDuplicateDetectionEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|cborXContent
operator|=
operator|new
name|CborXContent
argument_list|()
expr_stmt|;
block|}
DECL|method|CborXContent
specifier|private
name|CborXContent
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
name|CBOR
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
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"cbor does not support stream parsing..."
argument_list|)
throw|;
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
name|Set
argument_list|<
name|String
argument_list|>
name|includes
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|excludes
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|CborXContentGenerator
argument_list|(
name|cborFactory
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
name|os
argument_list|,
name|includes
argument_list|,
name|excludes
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
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|,
name|String
name|content
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|CborXContentParser
argument_list|(
name|xContentRegistry
argument_list|,
name|cborFactory
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
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|,
name|InputStream
name|is
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|CborXContentParser
argument_list|(
name|xContentRegistry
argument_list|,
name|cborFactory
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
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|,
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|CborXContentParser
argument_list|(
name|xContentRegistry
argument_list|,
name|cborFactory
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
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|,
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
name|CborXContentParser
argument_list|(
name|xContentRegistry
argument_list|,
name|cborFactory
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
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|,
name|BytesReference
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createParser
argument_list|(
name|xContentRegistry
argument_list|,
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
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|,
name|Reader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|CborXContentParser
argument_list|(
name|xContentRegistry
argument_list|,
name|cborFactory
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

