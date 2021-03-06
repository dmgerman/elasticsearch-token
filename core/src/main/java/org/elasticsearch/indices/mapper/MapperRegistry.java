begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|mapper
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|Mapper
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
name|mapper
operator|.
name|MetadataFieldMapper
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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

begin_comment
comment|/**  * A registry for all field mappers.  */
end_comment

begin_class
DECL|class|MapperRegistry
specifier|public
specifier|final
class|class
name|MapperRegistry
block|{
DECL|field|mapperParsers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Mapper
operator|.
name|TypeParser
argument_list|>
name|mapperParsers
decl_stmt|;
DECL|field|metadataMapperParsers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|MetadataFieldMapper
operator|.
name|TypeParser
argument_list|>
name|metadataMapperParsers
decl_stmt|;
DECL|method|MapperRegistry
specifier|public
name|MapperRegistry
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Mapper
operator|.
name|TypeParser
argument_list|>
name|mapperParsers
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|MetadataFieldMapper
operator|.
name|TypeParser
argument_list|>
name|metadataMapperParsers
parameter_list|)
block|{
name|this
operator|.
name|mapperParsers
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|(
name|mapperParsers
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|metadataMapperParsers
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|(
name|metadataMapperParsers
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Return a map of the mappers that have been registered. The      * returned map uses the type of the field as a key.      */
DECL|method|getMapperParsers
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Mapper
operator|.
name|TypeParser
argument_list|>
name|getMapperParsers
parameter_list|()
block|{
return|return
name|mapperParsers
return|;
block|}
comment|/**      * Return a map of the meta mappers that have been registered. The      * returned map uses the name of the field as a key.      */
DECL|method|getMetadataMapperParsers
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|MetadataFieldMapper
operator|.
name|TypeParser
argument_list|>
name|getMetadataMapperParsers
parameter_list|()
block|{
return|return
name|metadataMapperParsers
return|;
block|}
block|}
end_class

end_unit

