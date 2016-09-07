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
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|TokenStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|minhash
operator|.
name|MinHashFilterFactory
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
name|env
operator|.
name|Environment
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
name|IndexSettings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
comment|/**  * TokenFilterFactoryAdapter for {@link MinHashFilterFactory}  *  */
end_comment

begin_class
DECL|class|MinHashTokenFilterFactory
specifier|public
class|class
name|MinHashTokenFilterFactory
extends|extends
name|AbstractTokenFilterFactory
block|{
DECL|field|minHashFilterFactory
specifier|private
specifier|final
name|MinHashFilterFactory
name|minHashFilterFactory
decl_stmt|;
DECL|method|MinHashTokenFilterFactory
specifier|public
name|MinHashTokenFilterFactory
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|Environment
name|environment
parameter_list|,
name|String
name|name
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|indexSettings
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|minHashFilterFactory
operator|=
operator|new
name|MinHashFilterFactory
argument_list|(
name|convertSettings
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|)
block|{
return|return
name|minHashFilterFactory
operator|.
name|create
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
DECL|method|convertSettings
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|convertSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|settingMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|settingMap
operator|.
name|put
argument_list|(
literal|"hashCount"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"hash_count"
argument_list|)
argument_list|)
expr_stmt|;
name|settingMap
operator|.
name|put
argument_list|(
literal|"bucketCount"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"bucket_count"
argument_list|)
argument_list|)
expr_stmt|;
name|settingMap
operator|.
name|put
argument_list|(
literal|"hashSetSize"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"hash_set_size"
argument_list|)
argument_list|)
expr_stmt|;
name|settingMap
operator|.
name|put
argument_list|(
literal|"withRotation"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"with_rotation"
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|settingMap
return|;
block|}
block|}
end_class

end_unit

