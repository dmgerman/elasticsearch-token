begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.cache.field
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|cache
operator|.
name|field
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
name|cache
operator|.
name|field
operator|.
name|weak
operator|.
name|WeakFieldDataCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|inject
operator|.
name|AbstractModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|inject
operator|.
name|Scopes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|FieldDataCacheModule
specifier|public
class|class
name|FieldDataCacheModule
extends|extends
name|AbstractModule
block|{
DECL|class|FieldDataCacheSettings
specifier|public
specifier|static
specifier|final
class|class
name|FieldDataCacheSettings
block|{
DECL|field|FIELD_DATA_CACHE_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|FIELD_DATA_CACHE_TYPE
init|=
literal|"index.cache.field.type"
decl_stmt|;
block|}
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|method|FieldDataCacheModule
specifier|public
name|FieldDataCacheModule
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
block|}
DECL|method|configure
annotation|@
name|Override
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|bind
argument_list|(
name|FieldDataCache
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|settings
operator|.
name|getAsClass
argument_list|(
name|FieldDataCacheSettings
operator|.
name|FIELD_DATA_CACHE_TYPE
argument_list|,
name|WeakFieldDataCache
operator|.
name|class
argument_list|,
literal|"org.elasticsearch.index.cache.field."
argument_list|,
literal|"FieldDataCache"
argument_list|)
argument_list|)
operator|.
name|in
argument_list|(
name|Scopes
operator|.
name|SINGLETON
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

