begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.mapping.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|get
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectObjectCursor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|MappingMetaData
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
name|collect
operator|.
name|ImmutableOpenMap
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
name|stream
operator|.
name|StreamInput
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
name|stream
operator|.
name|StreamOutput
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

begin_class
DECL|class|GetMappingsResponse
specifier|public
class|class
name|GetMappingsResponse
extends|extends
name|ActionResponse
block|{
DECL|field|mappings
specifier|private
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|MappingMetaData
argument_list|>
argument_list|>
name|mappings
init|=
name|ImmutableOpenMap
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|method|GetMappingsResponse
name|GetMappingsResponse
parameter_list|(
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|MappingMetaData
argument_list|>
argument_list|>
name|mappings
parameter_list|)
block|{
name|this
operator|.
name|mappings
operator|=
name|mappings
expr_stmt|;
block|}
DECL|method|GetMappingsResponse
name|GetMappingsResponse
parameter_list|()
block|{     }
DECL|method|mappings
specifier|public
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|MappingMetaData
argument_list|>
argument_list|>
name|mappings
parameter_list|()
block|{
return|return
name|mappings
return|;
block|}
DECL|method|getMappings
specifier|public
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|MappingMetaData
argument_list|>
argument_list|>
name|getMappings
parameter_list|()
block|{
return|return
name|mappings
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|MappingMetaData
argument_list|>
argument_list|>
name|indexMapBuilder
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|int
name|valueSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|MappingMetaData
argument_list|>
name|typeMapBuilder
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|valueSize
condition|;
name|j
operator|++
control|)
block|{
name|typeMapBuilder
operator|.
name|put
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|,
name|MappingMetaData
operator|.
name|PROTO
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexMapBuilder
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|typeMapBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|mappings
operator|=
name|indexMapBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|mappings
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ObjectObjectCursor
argument_list|<
name|String
argument_list|,
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|MappingMetaData
argument_list|>
argument_list|>
name|indexEntry
range|:
name|mappings
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|indexEntry
operator|.
name|key
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|indexEntry
operator|.
name|value
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ObjectObjectCursor
argument_list|<
name|String
argument_list|,
name|MappingMetaData
argument_list|>
name|typeEntry
range|:
name|indexEntry
operator|.
name|value
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|typeEntry
operator|.
name|key
argument_list|)
expr_stmt|;
name|typeEntry
operator|.
name|value
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

