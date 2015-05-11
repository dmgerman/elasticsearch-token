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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|Strings
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
name|object
operator|.
name|ObjectMapper
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
name|object
operator|.
name|RootObjectMapper
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
name|util
operator|.
name|Collection
import|;
end_import

begin_enum
DECL|enum|MapperUtils
specifier|public
enum|enum
name|MapperUtils
block|{     ;
DECL|method|newStrictMergeResult
specifier|private
specifier|static
name|MergeResult
name|newStrictMergeResult
parameter_list|()
block|{
return|return
operator|new
name|MergeResult
argument_list|(
literal|false
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|hasConflicts
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|buildConflicts
parameter_list|()
block|{
return|return
name|Strings
operator|.
name|EMPTY_ARRAY
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addFieldMappers
parameter_list|(
name|Collection
argument_list|<
name|FieldMapper
argument_list|<
name|?
argument_list|>
argument_list|>
name|fieldMappers
parameter_list|)
block|{
comment|// no-op
block|}
annotation|@
name|Override
specifier|public
name|void
name|addObjectMappers
parameter_list|(
name|Collection
argument_list|<
name|ObjectMapper
argument_list|>
name|objectMappers
parameter_list|)
block|{
comment|// no-op
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|FieldMapper
argument_list|<
name|?
argument_list|>
argument_list|>
name|getNewFieldMappers
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Strict merge result does not support new field mappers"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|ObjectMapper
argument_list|>
name|getNewObjectMappers
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Strict merge result does not support new object mappers"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addConflict
parameter_list|(
name|String
name|mergeFailure
parameter_list|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Merging dynamic updates triggered a conflict: "
operator|+
name|mergeFailure
argument_list|)
throw|;
block|}
block|}
return|;
block|}
comment|/**      * Merge {@code mergeWith} into {@code mergeTo}. Note: this method only      * merges mappings, not lookup structures. Conflicts are returned as exceptions.      */
DECL|method|merge
specifier|public
specifier|static
name|void
name|merge
parameter_list|(
name|Mapper
name|mergeInto
parameter_list|,
name|Mapper
name|mergeWith
parameter_list|)
block|{
name|mergeInto
operator|.
name|merge
argument_list|(
name|mergeWith
argument_list|,
name|newStrictMergeResult
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Merge {@code mergeWith} into {@code mergeTo}. Note: this method only      * merges mappings, not lookup structures. Conflicts are returned as exceptions.      */
DECL|method|merge
specifier|public
specifier|static
name|void
name|merge
parameter_list|(
name|Mapping
name|mergeInto
parameter_list|,
name|Mapping
name|mergeWith
parameter_list|)
block|{
name|mergeInto
operator|.
name|merge
argument_list|(
name|mergeWith
argument_list|,
name|newStrictMergeResult
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Split mapper and its descendants into object and field mappers. */
DECL|method|collect
specifier|public
specifier|static
name|void
name|collect
parameter_list|(
name|Mapper
name|mapper
parameter_list|,
name|Collection
argument_list|<
name|ObjectMapper
argument_list|>
name|objectMappers
parameter_list|,
name|Collection
argument_list|<
name|FieldMapper
argument_list|<
name|?
argument_list|>
argument_list|>
name|fieldMappers
parameter_list|)
block|{
if|if
condition|(
name|mapper
operator|instanceof
name|RootObjectMapper
condition|)
block|{
comment|// root mapper isn't really an object mapper
block|}
elseif|else
if|if
condition|(
name|mapper
operator|instanceof
name|ObjectMapper
condition|)
block|{
name|objectMappers
operator|.
name|add
argument_list|(
operator|(
name|ObjectMapper
operator|)
name|mapper
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|mapper
operator|instanceof
name|FieldMapper
argument_list|<
name|?
argument_list|>
condition|)
block|{
name|fieldMappers
operator|.
name|add
argument_list|(
operator|(
name|FieldMapper
argument_list|<
name|?
argument_list|>
operator|)
name|mapper
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Mapper
name|child
range|:
name|mapper
control|)
block|{
name|collect
argument_list|(
name|child
argument_list|,
name|objectMappers
argument_list|,
name|fieldMappers
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_enum

end_unit

