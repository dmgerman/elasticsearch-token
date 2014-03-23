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
name|dataformat
operator|.
name|cbor
operator|.
name|CBORParser
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
name|XContentType
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
name|JsonXContentGenerator
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|CborXContentGenerator
specifier|public
class|class
name|CborXContentGenerator
extends|extends
name|JsonXContentGenerator
block|{
DECL|method|CborXContentGenerator
specifier|public
name|CborXContentGenerator
parameter_list|(
name|JsonGenerator
name|generator
parameter_list|)
block|{
name|super
argument_list|(
name|generator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|contentType
specifier|public
name|XContentType
name|contentType
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
DECL|method|usePrintLineFeedAtEnd
specifier|public
name|void
name|usePrintLineFeedAtEnd
parameter_list|()
block|{
comment|// nothing here
block|}
annotation|@
name|Override
DECL|method|writeRawField
specifier|public
name|void
name|writeRawField
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|InputStream
name|content
parameter_list|,
name|OutputStream
name|bos
parameter_list|)
throws|throws
name|IOException
block|{
name|writeFieldName
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
try|try
init|(
name|CBORParser
name|parser
init|=
name|CborXContent
operator|.
name|cborFactory
operator|.
name|createParser
argument_list|(
name|content
argument_list|)
init|)
block|{
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|generator
operator|.
name|copyCurrentStructure
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRawField
specifier|public
name|void
name|writeRawField
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|byte
index|[]
name|content
parameter_list|,
name|OutputStream
name|bos
parameter_list|)
throws|throws
name|IOException
block|{
name|writeFieldName
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
try|try
init|(
name|CBORParser
name|parser
init|=
name|CborXContent
operator|.
name|cborFactory
operator|.
name|createParser
argument_list|(
name|content
argument_list|)
init|)
block|{
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|generator
operator|.
name|copyCurrentStructure
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeObjectRaw
specifier|protected
name|void
name|writeObjectRaw
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|BytesReference
name|content
parameter_list|,
name|OutputStream
name|bos
parameter_list|)
throws|throws
name|IOException
block|{
name|writeFieldName
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|CBORParser
name|parser
decl_stmt|;
if|if
condition|(
name|content
operator|.
name|hasArray
argument_list|()
condition|)
block|{
name|parser
operator|=
name|CborXContent
operator|.
name|cborFactory
operator|.
name|createParser
argument_list|(
name|content
operator|.
name|array
argument_list|()
argument_list|,
name|content
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|content
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|parser
operator|=
name|CborXContent
operator|.
name|cborFactory
operator|.
name|createParser
argument_list|(
name|content
operator|.
name|streamInput
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|generator
operator|.
name|copyCurrentStructure
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|parser
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRawField
specifier|public
name|void
name|writeRawField
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|byte
index|[]
name|content
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|OutputStream
name|bos
parameter_list|)
throws|throws
name|IOException
block|{
name|writeFieldName
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
try|try
init|(
name|CBORParser
name|parser
init|=
name|CborXContent
operator|.
name|cborFactory
operator|.
name|createParser
argument_list|(
name|content
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
init|)
block|{
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|generator
operator|.
name|copyCurrentStructure
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

