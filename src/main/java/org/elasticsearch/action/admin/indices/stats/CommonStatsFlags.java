begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.stats
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
name|stats
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|Streamable
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
name|EnumSet
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|CommonStatsFlags
specifier|public
class|class
name|CommonStatsFlags
implements|implements
name|Streamable
implements|,
name|Cloneable
block|{
DECL|field|ALL
specifier|public
specifier|final
specifier|static
name|CommonStatsFlags
name|ALL
init|=
operator|new
name|CommonStatsFlags
argument_list|()
operator|.
name|all
argument_list|()
decl_stmt|;
DECL|field|NONE
specifier|public
specifier|final
specifier|static
name|CommonStatsFlags
name|NONE
init|=
operator|new
name|CommonStatsFlags
argument_list|()
operator|.
name|clear
argument_list|()
decl_stmt|;
DECL|field|flags
specifier|private
name|EnumSet
argument_list|<
name|Flag
argument_list|>
name|flags
init|=
name|EnumSet
operator|.
name|allOf
argument_list|(
name|Flag
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|types
specifier|private
name|String
index|[]
name|types
init|=
literal|null
decl_stmt|;
DECL|field|groups
specifier|private
name|String
index|[]
name|groups
init|=
literal|null
decl_stmt|;
DECL|field|fieldDataFields
specifier|private
name|String
index|[]
name|fieldDataFields
init|=
literal|null
decl_stmt|;
DECL|field|completionDataFields
specifier|private
name|String
index|[]
name|completionDataFields
init|=
literal|null
decl_stmt|;
comment|/**      * @param flags flags to set. If no flags are supplied, default flags will be set.      */
DECL|method|CommonStatsFlags
specifier|public
name|CommonStatsFlags
parameter_list|(
name|Flag
modifier|...
name|flags
parameter_list|)
block|{
if|if
condition|(
name|flags
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Flag
name|f
range|:
name|flags
control|)
block|{
name|this
operator|.
name|flags
operator|.
name|add
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Sets all flags to return all stats.      */
DECL|method|all
specifier|public
name|CommonStatsFlags
name|all
parameter_list|()
block|{
name|flags
operator|=
name|EnumSet
operator|.
name|allOf
argument_list|(
name|Flag
operator|.
name|class
argument_list|)
expr_stmt|;
name|types
operator|=
literal|null
expr_stmt|;
name|groups
operator|=
literal|null
expr_stmt|;
name|fieldDataFields
operator|=
literal|null
expr_stmt|;
name|completionDataFields
operator|=
literal|null
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Clears all stats.      */
DECL|method|clear
specifier|public
name|CommonStatsFlags
name|clear
parameter_list|()
block|{
name|flags
operator|=
name|EnumSet
operator|.
name|noneOf
argument_list|(
name|Flag
operator|.
name|class
argument_list|)
expr_stmt|;
name|types
operator|=
literal|null
expr_stmt|;
name|groups
operator|=
literal|null
expr_stmt|;
name|fieldDataFields
operator|=
literal|null
expr_stmt|;
name|completionDataFields
operator|=
literal|null
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|anySet
specifier|public
name|boolean
name|anySet
parameter_list|()
block|{
return|return
operator|!
name|flags
operator|.
name|isEmpty
argument_list|()
return|;
block|}
DECL|method|getFlags
specifier|public
name|Flag
index|[]
name|getFlags
parameter_list|()
block|{
return|return
name|flags
operator|.
name|toArray
argument_list|(
operator|new
name|Flag
index|[
name|flags
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
comment|/**      * Document types to return stats for. Mainly affects {@link Flag#Indexing} when      * enabled, returning specific indexing stats for those types.      */
DECL|method|types
specifier|public
name|CommonStatsFlags
name|types
parameter_list|(
name|String
modifier|...
name|types
parameter_list|)
block|{
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Document types to return stats for. Mainly affects {@link Flag#Indexing} when      * enabled, returning specific indexing stats for those types.      */
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
name|this
operator|.
name|types
return|;
block|}
comment|/**      * Sets specific search group stats to retrieve the stats for. Mainly affects search      * when enabled.      */
DECL|method|groups
specifier|public
name|CommonStatsFlags
name|groups
parameter_list|(
name|String
modifier|...
name|groups
parameter_list|)
block|{
name|this
operator|.
name|groups
operator|=
name|groups
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|groups
specifier|public
name|String
index|[]
name|groups
parameter_list|()
block|{
return|return
name|this
operator|.
name|groups
return|;
block|}
comment|/**      * Sets specific search group stats to retrieve the stats for. Mainly affects search      * when enabled.      */
DECL|method|fieldDataFields
specifier|public
name|CommonStatsFlags
name|fieldDataFields
parameter_list|(
name|String
modifier|...
name|fieldDataFields
parameter_list|)
block|{
name|this
operator|.
name|fieldDataFields
operator|=
name|fieldDataFields
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|fieldDataFields
specifier|public
name|String
index|[]
name|fieldDataFields
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldDataFields
return|;
block|}
DECL|method|completionDataFields
specifier|public
name|CommonStatsFlags
name|completionDataFields
parameter_list|(
name|String
modifier|...
name|completionDataFields
parameter_list|)
block|{
name|this
operator|.
name|completionDataFields
operator|=
name|completionDataFields
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|completionDataFields
specifier|public
name|String
index|[]
name|completionDataFields
parameter_list|()
block|{
return|return
name|this
operator|.
name|completionDataFields
return|;
block|}
DECL|method|isSet
specifier|public
name|boolean
name|isSet
parameter_list|(
name|Flag
name|flag
parameter_list|)
block|{
return|return
name|flags
operator|.
name|contains
argument_list|(
name|flag
argument_list|)
return|;
block|}
DECL|method|unSet
name|boolean
name|unSet
parameter_list|(
name|Flag
name|flag
parameter_list|)
block|{
return|return
name|flags
operator|.
name|remove
argument_list|(
name|flag
argument_list|)
return|;
block|}
DECL|method|set
name|void
name|set
parameter_list|(
name|Flag
name|flag
parameter_list|)
block|{
name|flags
operator|.
name|add
argument_list|(
name|flag
argument_list|)
expr_stmt|;
block|}
DECL|method|set
specifier|public
name|CommonStatsFlags
name|set
parameter_list|(
name|Flag
name|flag
parameter_list|,
name|boolean
name|add
parameter_list|)
block|{
if|if
condition|(
name|add
condition|)
block|{
name|set
argument_list|(
name|flag
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|unSet
argument_list|(
name|flag
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|readCommonStatsFlags
specifier|public
specifier|static
name|CommonStatsFlags
name|readCommonStatsFlags
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|CommonStatsFlags
name|flags
init|=
operator|new
name|CommonStatsFlags
argument_list|()
decl_stmt|;
name|flags
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|flags
return|;
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
name|long
name|longFlags
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Flag
name|flag
range|:
name|flags
control|)
block|{
name|longFlags
operator||=
operator|(
literal|1
operator|<<
name|flag
operator|.
name|ordinal
argument_list|()
operator|)
expr_stmt|;
block|}
name|out
operator|.
name|writeLong
argument_list|(
name|longFlags
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArrayNullable
argument_list|(
name|types
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArrayNullable
argument_list|(
name|groups
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArrayNullable
argument_list|(
name|fieldDataFields
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_0_90_4
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeStringArrayNullable
argument_list|(
name|completionDataFields
argument_list|)
expr_stmt|;
block|}
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
specifier|final
name|long
name|longFlags
init|=
name|in
operator|.
name|readLong
argument_list|()
decl_stmt|;
name|flags
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Flag
name|flag
range|:
name|Flag
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
operator|(
name|longFlags
operator|&
operator|(
literal|1
operator|<<
name|flag
operator|.
name|ordinal
argument_list|()
operator|)
operator|)
operator|!=
literal|0
condition|)
block|{
name|flags
operator|.
name|add
argument_list|(
name|flag
argument_list|)
expr_stmt|;
block|}
block|}
name|types
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|groups
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|fieldDataFields
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_0_90_4
argument_list|)
condition|)
block|{
name|completionDataFields
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|clone
specifier|public
name|CommonStatsFlags
name|clone
parameter_list|()
block|{
try|try
block|{
name|CommonStatsFlags
name|cloned
init|=
operator|(
name|CommonStatsFlags
operator|)
name|super
operator|.
name|clone
argument_list|()
decl_stmt|;
name|cloned
operator|.
name|flags
operator|=
name|flags
operator|.
name|clone
argument_list|()
expr_stmt|;
return|return
name|cloned
return|;
block|}
catch|catch
parameter_list|(
name|CloneNotSupportedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
DECL|enum|Flag
specifier|public
specifier|static
enum|enum
name|Flag
block|{
comment|// Do not change the order of these flags we use
comment|// the ordinal for encoding! Only append to the end!
DECL|enum constant|Store
name|Store
argument_list|(
literal|"store"
argument_list|)
block|,
DECL|enum constant|Indexing
name|Indexing
argument_list|(
literal|"indexing"
argument_list|)
block|,
DECL|enum constant|Get
name|Get
argument_list|(
literal|"get"
argument_list|)
block|,
DECL|enum constant|Search
name|Search
argument_list|(
literal|"search"
argument_list|)
block|,
DECL|enum constant|Merge
name|Merge
argument_list|(
literal|"merge"
argument_list|)
block|,
DECL|enum constant|Flush
name|Flush
argument_list|(
literal|"flush"
argument_list|)
block|,
DECL|enum constant|Refresh
name|Refresh
argument_list|(
literal|"refresh"
argument_list|)
block|,
DECL|enum constant|FilterCache
name|FilterCache
argument_list|(
literal|"filter_cache"
argument_list|)
block|,
DECL|enum constant|IdCache
name|IdCache
argument_list|(
literal|"id_cache"
argument_list|)
block|,
DECL|enum constant|FieldData
name|FieldData
argument_list|(
literal|"fielddata"
argument_list|)
block|,
DECL|enum constant|Docs
name|Docs
argument_list|(
literal|"docs"
argument_list|)
block|,
DECL|enum constant|Warmer
name|Warmer
argument_list|(
literal|"warmer"
argument_list|)
block|,
DECL|enum constant|Percolate
name|Percolate
argument_list|(
literal|"percolate"
argument_list|)
block|,
DECL|enum constant|Completion
name|Completion
argument_list|(
literal|"completion"
argument_list|)
block|,
DECL|enum constant|Segments
name|Segments
argument_list|(
literal|"segments"
argument_list|)
block|,
DECL|enum constant|Translog
name|Translog
argument_list|(
literal|"translog"
argument_list|)
block|;
DECL|field|restName
specifier|private
specifier|final
name|String
name|restName
decl_stmt|;
DECL|method|Flag
name|Flag
parameter_list|(
name|String
name|restName
parameter_list|)
block|{
name|this
operator|.
name|restName
operator|=
name|restName
expr_stmt|;
block|}
DECL|method|getRestName
specifier|public
name|String
name|getRestName
parameter_list|()
block|{
return|return
name|restName
return|;
block|}
block|}
block|}
end_class

end_unit

