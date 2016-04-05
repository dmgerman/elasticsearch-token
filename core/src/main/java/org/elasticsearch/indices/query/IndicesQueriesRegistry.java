begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|query
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
name|ParseField
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
name|ParseFieldMatcher
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
name|ParsingException
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
name|Tuple
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
name|component
operator|.
name|AbstractComponent
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
name|common
operator|.
name|xcontent
operator|.
name|XContentLocation
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
name|query
operator|.
name|QueryParser
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

begin_class
DECL|class|IndicesQueriesRegistry
specifier|public
class|class
name|IndicesQueriesRegistry
extends|extends
name|AbstractComponent
block|{
DECL|field|queryParsers
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Tuple
argument_list|<
name|ParseField
argument_list|,
name|QueryParser
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|queryParsers
decl_stmt|;
DECL|method|IndicesQueriesRegistry
specifier|public
name|IndicesQueriesRegistry
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Tuple
argument_list|<
name|ParseField
argument_list|,
name|QueryParser
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|queryParsers
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|queryParsers
operator|=
name|queryParsers
expr_stmt|;
block|}
comment|/**      * Get the query parser for a specific type of query registered under its name.      * Uses {@link ParseField} internally so that deprecation warnings/errors can be logged/thrown.      * @param name the name of the parser to retrieve      * @param parseFieldMatcher the {@link ParseFieldMatcher} to match the query name against      * @param xContentLocation the current location of the {@link org.elasticsearch.common.xcontent.XContentParser}      * @return the query parser      * @throws IllegalArgumentException of there's no query or parser registered under the provided name      */
DECL|method|getQueryParser
specifier|public
name|QueryParser
argument_list|<
name|?
argument_list|>
name|getQueryParser
parameter_list|(
name|String
name|name
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|,
name|XContentLocation
name|xContentLocation
parameter_list|)
block|{
name|Tuple
argument_list|<
name|ParseField
argument_list|,
name|QueryParser
argument_list|<
name|?
argument_list|>
argument_list|>
name|parseFieldQueryParserTuple
init|=
name|queryParsers
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|parseFieldQueryParserTuple
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|xContentLocation
argument_list|,
literal|"No query registered for ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|ParseField
name|parseField
init|=
name|parseFieldQueryParserTuple
operator|.
name|v1
argument_list|()
decl_stmt|;
name|QueryParser
argument_list|<
name|?
argument_list|>
name|queryParser
init|=
name|parseFieldQueryParserTuple
operator|.
name|v2
argument_list|()
decl_stmt|;
name|boolean
name|match
init|=
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|name
argument_list|,
name|parseField
argument_list|)
decl_stmt|;
comment|//this is always expected to match, ParseField is useful for deprecation warnings etc. here
assert|assert
name|match
operator|:
literal|"registered ParseField did not match the query name it was registered for: ["
operator|+
name|name
operator|+
literal|"]"
assert|;
return|return
name|queryParser
return|;
block|}
block|}
end_class

end_unit

