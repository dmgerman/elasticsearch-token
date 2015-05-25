begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.analysis
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|analysis
operator|.
name|PreBuiltTokenizers
import|;
end_import

begin_class
DECL|class|PreBuiltTokenizerFactoryFactory
specifier|public
class|class
name|PreBuiltTokenizerFactoryFactory
implements|implements
name|TokenizerFactoryFactory
block|{
DECL|field|tokenizerFactory
specifier|private
specifier|final
name|TokenizerFactory
name|tokenizerFactory
decl_stmt|;
DECL|method|PreBuiltTokenizerFactoryFactory
specifier|public
name|PreBuiltTokenizerFactoryFactory
parameter_list|(
name|TokenizerFactory
name|tokenizerFactory
parameter_list|)
block|{
name|this
operator|.
name|tokenizerFactory
operator|=
name|tokenizerFactory
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|TokenizerFactory
name|create
parameter_list|(
name|String
name|name
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|Version
name|indexVersion
init|=
name|Version
operator|.
name|indexCreated
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Version
operator|.
name|CURRENT
operator|.
name|equals
argument_list|(
name|indexVersion
argument_list|)
condition|)
block|{
name|PreBuiltTokenizers
name|preBuiltTokenizers
init|=
name|PreBuiltTokenizers
operator|.
name|getOrDefault
argument_list|(
name|name
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|preBuiltTokenizers
operator|!=
literal|null
condition|)
block|{
return|return
name|preBuiltTokenizers
operator|.
name|getTokenizerFactory
argument_list|(
name|indexVersion
argument_list|)
return|;
block|}
block|}
return|return
name|tokenizerFactory
return|;
block|}
block|}
end_class

end_unit

