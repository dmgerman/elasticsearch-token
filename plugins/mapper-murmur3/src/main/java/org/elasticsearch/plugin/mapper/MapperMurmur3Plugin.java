begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|mapper
package|;
end_package

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
name|Map
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
name|murmur3
operator|.
name|Murmur3FieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|MapperPlugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_class
DECL|class|MapperMurmur3Plugin
specifier|public
class|class
name|MapperMurmur3Plugin
extends|extends
name|Plugin
implements|implements
name|MapperPlugin
block|{
annotation|@
name|Override
DECL|method|getMappers
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Mapper
operator|.
name|TypeParser
argument_list|>
name|getMappers
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singletonMap
argument_list|(
name|Murmur3FieldMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|Murmur3FieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

