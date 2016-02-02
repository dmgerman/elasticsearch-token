begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
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
name|common
operator|.
name|util
operator|.
name|ExtensionPoint
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
operator|.
name|CompletionSuggester
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|phrase
operator|.
name|PhraseSuggester
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|term
operator|.
name|TermSuggester
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|HashSet
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
comment|/**  *  */
end_comment

begin_class
DECL|class|Suggesters
specifier|public
specifier|final
class|class
name|Suggesters
extends|extends
name|ExtensionPoint
operator|.
name|ClassMap
argument_list|<
name|Suggester
argument_list|>
block|{
DECL|field|parsers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Suggester
argument_list|>
name|parsers
decl_stmt|;
DECL|method|Suggesters
specifier|public
name|Suggesters
parameter_list|()
block|{
name|this
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Inject
DECL|method|Suggesters
specifier|public
name|Suggesters
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Suggester
argument_list|>
name|suggesters
parameter_list|)
block|{
name|super
argument_list|(
literal|"suggester"
argument_list|,
name|Suggester
operator|.
name|class
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"phrase"
argument_list|,
literal|"term"
argument_list|,
literal|"completion"
argument_list|)
argument_list|)
argument_list|,
name|Suggesters
operator|.
name|class
argument_list|,
name|SuggestParseElement
operator|.
name|class
argument_list|,
name|SuggestPhase
operator|.
name|class
argument_list|)
expr_stmt|;
name|this
operator|.
name|parsers
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|addBuildIns
argument_list|(
name|suggesters
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|addBuildIns
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Suggester
argument_list|>
name|addBuildIns
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Suggester
argument_list|>
name|suggesters
parameter_list|)
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Suggester
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"phrase"
argument_list|,
name|PhraseSuggester
operator|.
name|PROTOTYPE
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"term"
argument_list|,
name|TermSuggester
operator|.
name|PROTOTYPE
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"completion"
argument_list|,
name|CompletionSuggester
operator|.
name|PROTOTYPE
argument_list|)
expr_stmt|;
name|map
operator|.
name|putAll
argument_list|(
name|suggesters
argument_list|)
expr_stmt|;
return|return
name|map
return|;
block|}
DECL|method|get
specifier|public
name|Suggester
name|get
parameter_list|(
name|String
name|type
parameter_list|)
block|{
return|return
name|parsers
operator|.
name|get
argument_list|(
name|type
argument_list|)
return|;
block|}
DECL|method|getSuggestionPrototype
specifier|public
name|SuggestionBuilder
argument_list|<
name|?
argument_list|>
name|getSuggestionPrototype
parameter_list|(
name|String
name|suggesterName
parameter_list|)
block|{
name|Suggester
argument_list|<
name|?
argument_list|>
name|suggester
init|=
name|parsers
operator|.
name|get
argument_list|(
name|suggesterName
argument_list|)
decl_stmt|;
if|if
condition|(
name|suggester
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"suggester with name ["
operator|+
name|suggesterName
operator|+
literal|"] not supported"
argument_list|)
throw|;
block|}
name|SuggestionBuilder
argument_list|<
name|?
argument_list|>
name|suggestParser
init|=
name|suggester
operator|.
name|getBuilderPrototype
argument_list|()
decl_stmt|;
if|if
condition|(
name|suggestParser
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"suggester with name ["
operator|+
name|suggesterName
operator|+
literal|"] not supported"
argument_list|)
throw|;
block|}
return|return
name|suggestParser
return|;
block|}
block|}
end_class

end_unit

