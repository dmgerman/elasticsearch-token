begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.json
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|json
package|;
end_package

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|JsonMapperBuilders
specifier|public
specifier|final
class|class
name|JsonMapperBuilders
block|{
DECL|method|JsonMapperBuilders
specifier|private
name|JsonMapperBuilders
parameter_list|()
block|{      }
DECL|method|doc
specifier|public
specifier|static
name|JsonDocumentMapper
operator|.
name|Builder
name|doc
parameter_list|(
name|JsonObjectMapper
operator|.
name|Builder
name|objectBuilder
parameter_list|)
block|{
return|return
operator|new
name|JsonDocumentMapper
operator|.
name|Builder
argument_list|(
name|objectBuilder
argument_list|)
return|;
block|}
DECL|method|source
specifier|public
specifier|static
name|JsonSourceFieldMapper
operator|.
name|Builder
name|source
parameter_list|()
block|{
return|return
operator|new
name|JsonSourceFieldMapper
operator|.
name|Builder
argument_list|()
return|;
block|}
DECL|method|id
specifier|public
specifier|static
name|JsonIdFieldMapper
operator|.
name|Builder
name|id
parameter_list|()
block|{
return|return
operator|new
name|JsonIdFieldMapper
operator|.
name|Builder
argument_list|()
return|;
block|}
DECL|method|uid
specifier|public
specifier|static
name|JsonUidFieldMapper
operator|.
name|Builder
name|uid
parameter_list|()
block|{
return|return
operator|new
name|JsonUidFieldMapper
operator|.
name|Builder
argument_list|()
return|;
block|}
DECL|method|type
specifier|public
specifier|static
name|JsonTypeFieldMapper
operator|.
name|Builder
name|type
parameter_list|()
block|{
return|return
operator|new
name|JsonTypeFieldMapper
operator|.
name|Builder
argument_list|()
return|;
block|}
DECL|method|boost
specifier|public
specifier|static
name|JsonBoostFieldMapper
operator|.
name|Builder
name|boost
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|JsonBoostFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|object
specifier|public
specifier|static
name|JsonObjectMapper
operator|.
name|Builder
name|object
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|JsonObjectMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|booleanField
specifier|public
specifier|static
name|JsonBooleanFieldMapper
operator|.
name|Builder
name|booleanField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|JsonBooleanFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|stringField
specifier|public
specifier|static
name|JsonStringFieldMapper
operator|.
name|Builder
name|stringField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|JsonStringFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|binaryField
specifier|public
specifier|static
name|JsonBinaryFieldMapper
operator|.
name|Builder
name|binaryField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|JsonBinaryFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|dateField
specifier|public
specifier|static
name|JsonDateFieldMapper
operator|.
name|Builder
name|dateField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|JsonDateFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|integerField
specifier|public
specifier|static
name|JsonIntegerFieldMapper
operator|.
name|Builder
name|integerField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|JsonIntegerFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|longField
specifier|public
specifier|static
name|JsonLongFieldMapper
operator|.
name|Builder
name|longField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|JsonLongFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|floatField
specifier|public
specifier|static
name|JsonFloatFieldMapper
operator|.
name|Builder
name|floatField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|JsonFloatFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|doubleField
specifier|public
specifier|static
name|JsonDoubleFieldMapper
operator|.
name|Builder
name|doubleField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|JsonDoubleFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
block|}
end_class

end_unit

