begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.ordinals
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|ordinals
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|LongsRef
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalStateException
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_enum
DECL|enum|EmptyOrdinals
specifier|public
enum|enum
name|EmptyOrdinals
implements|implements
name|Ordinals
block|{
DECL|enum constant|INSTANCE
name|INSTANCE
block|;
annotation|@
name|Override
DECL|method|getMemorySizeInBytes
specifier|public
name|long
name|getMemorySizeInBytes
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|isMultiValued
specifier|public
name|boolean
name|isMultiValued
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|getMaxOrd
specifier|public
name|long
name|getMaxOrd
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
annotation|@
name|Override
DECL|method|ordinals
specifier|public
name|Docs
name|ordinals
parameter_list|()
block|{
return|return
operator|new
name|Docs
argument_list|(
name|this
argument_list|)
return|;
block|}
DECL|class|Docs
specifier|public
specifier|static
class|class
name|Docs
extends|extends
name|Ordinals
operator|.
name|AbstractDocs
block|{
DECL|field|EMPTY_LONGS_REF
specifier|public
specifier|static
specifier|final
name|LongsRef
name|EMPTY_LONGS_REF
init|=
operator|new
name|LongsRef
argument_list|()
decl_stmt|;
DECL|method|Docs
specifier|public
name|Docs
parameter_list|(
name|EmptyOrdinals
name|parent
parameter_list|)
block|{
name|super
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getOrd
specifier|public
name|long
name|getOrd
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
name|Ordinals
operator|.
name|MISSING_ORDINAL
return|;
block|}
annotation|@
name|Override
DECL|method|nextOrd
specifier|public
name|long
name|nextOrd
parameter_list|()
block|{
throw|throw
operator|new
name|ElasticsearchIllegalStateException
argument_list|(
literal|"Empty ordinals has no nextOrd"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|setDocument
specifier|public
name|int
name|setDocument
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|currentOrd
specifier|public
name|long
name|currentOrd
parameter_list|()
block|{
return|return
name|Ordinals
operator|.
name|MISSING_ORDINAL
return|;
block|}
block|}
block|}
end_enum

end_unit

