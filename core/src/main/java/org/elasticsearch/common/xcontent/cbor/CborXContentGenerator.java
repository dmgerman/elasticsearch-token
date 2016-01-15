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
name|jsonGenerator
parameter_list|,
name|OutputStream
name|os
parameter_list|,
name|String
modifier|...
name|filters
parameter_list|)
block|{
name|this
argument_list|(
name|jsonGenerator
argument_list|,
name|os
argument_list|,
literal|true
argument_list|,
name|filters
argument_list|)
expr_stmt|;
block|}
DECL|method|CborXContentGenerator
specifier|public
name|CborXContentGenerator
parameter_list|(
name|JsonGenerator
name|jsonGenerator
parameter_list|,
name|OutputStream
name|os
parameter_list|,
name|boolean
name|inclusive
parameter_list|,
name|String
index|[]
name|filters
parameter_list|)
block|{
name|super
argument_list|(
name|jsonGenerator
argument_list|,
name|os
argument_list|,
name|inclusive
argument_list|,
name|filters
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
DECL|method|supportsRawWrites
specifier|protected
name|boolean
name|supportsRawWrites
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

