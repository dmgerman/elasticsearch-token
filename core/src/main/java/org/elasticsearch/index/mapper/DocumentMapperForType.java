begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
package|;
end_package

begin_class
DECL|class|DocumentMapperForType
specifier|public
class|class
name|DocumentMapperForType
block|{
DECL|field|documentMapper
specifier|private
specifier|final
name|DocumentMapper
name|documentMapper
decl_stmt|;
DECL|field|mapping
specifier|private
specifier|final
name|Mapping
name|mapping
decl_stmt|;
DECL|method|DocumentMapperForType
specifier|public
name|DocumentMapperForType
parameter_list|(
name|DocumentMapper
name|documentMapper
parameter_list|,
name|Mapping
name|mapping
parameter_list|)
block|{
name|this
operator|.
name|mapping
operator|=
name|mapping
expr_stmt|;
name|this
operator|.
name|documentMapper
operator|=
name|documentMapper
expr_stmt|;
block|}
DECL|method|getDocumentMapper
specifier|public
name|DocumentMapper
name|getDocumentMapper
parameter_list|()
block|{
return|return
name|documentMapper
return|;
block|}
DECL|method|getMapping
specifier|public
name|Mapping
name|getMapping
parameter_list|()
block|{
return|return
name|mapping
return|;
block|}
block|}
end_class

end_unit

