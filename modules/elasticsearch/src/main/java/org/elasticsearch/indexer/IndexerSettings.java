begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indexer
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indexer
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
name|settings
operator|.
name|Settings
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
comment|/**  * @author kimchy (shayy.banon)  */
end_comment

begin_class
DECL|class|IndexerSettings
specifier|public
class|class
name|IndexerSettings
block|{
DECL|field|globalSettings
specifier|private
specifier|final
name|Settings
name|globalSettings
decl_stmt|;
DECL|field|settings
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|settings
decl_stmt|;
DECL|method|IndexerSettings
specifier|public
name|IndexerSettings
parameter_list|(
name|Settings
name|globalSettings
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|settings
parameter_list|)
block|{
name|this
operator|.
name|globalSettings
operator|=
name|globalSettings
expr_stmt|;
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
block|}
DECL|method|globalSettings
specifier|public
name|Settings
name|globalSettings
parameter_list|()
block|{
return|return
name|globalSettings
return|;
block|}
DECL|method|settings
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|settings
parameter_list|()
block|{
return|return
name|settings
return|;
block|}
block|}
end_class

end_unit

