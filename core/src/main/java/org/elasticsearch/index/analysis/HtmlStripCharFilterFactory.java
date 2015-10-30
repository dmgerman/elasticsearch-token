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
name|charfilter
operator|.
name|HTMLStripCharFilter
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
name|inject
operator|.
name|Inject
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
name|io
operator|.
name|Reader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableSet
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|set
operator|.
name|Sets
operator|.
name|newHashSet
import|;
end_import

begin_class
DECL|class|HtmlStripCharFilterFactory
specifier|public
class|class
name|HtmlStripCharFilterFactory
extends|extends
name|AbstractCharFilterFactory
block|{
DECL|field|escapedTags
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|escapedTags
decl_stmt|;
DECL|method|HtmlStripCharFilterFactory
specifier|public
name|HtmlStripCharFilterFactory
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|Environment
name|env
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
argument_list|)
expr_stmt|;
name|String
index|[]
name|escapedTags
init|=
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"escaped_tags"
argument_list|)
decl_stmt|;
if|if
condition|(
name|escapedTags
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|escapedTags
operator|=
name|unmodifiableSet
argument_list|(
name|newHashSet
argument_list|(
name|escapedTags
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|escapedTags
operator|=
literal|null
expr_stmt|;
block|}
block|}
DECL|method|escapedTags
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|escapedTags
parameter_list|()
block|{
return|return
name|escapedTags
return|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|Reader
name|create
parameter_list|(
name|Reader
name|tokenStream
parameter_list|)
block|{
return|return
operator|new
name|HTMLStripCharFilter
argument_list|(
name|tokenStream
argument_list|,
name|escapedTags
argument_list|)
return|;
block|}
block|}
end_class

end_unit

