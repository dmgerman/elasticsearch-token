begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.context
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|context
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|search
operator|.
name|suggest
operator|.
name|analyzing
operator|.
name|XAnalyzingSuggester
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
name|util
operator|.
name|automaton
operator|.
name|Automata
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
name|util
operator|.
name|automaton
operator|.
name|Automaton
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
name|util
operator|.
name|automaton
operator|.
name|Operations
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
name|util
operator|.
name|fst
operator|.
name|FST
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|ToXContent
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
name|XContentBuilder
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
name|XContentParser
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
name|XContentParser
operator|.
name|Token
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
name|json
operator|.
name|JsonXContent
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
name|ParseContext
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
name|ParseContext
operator|.
name|Document
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
name|*
import|;
end_import

begin_comment
comment|/**  * A {@link ContextMapping} is used t define a context that may used  * in conjunction with a suggester. To define a suggester that depends on a  * specific context derived class of {@link ContextMapping} will be  * used to specify the kind of additional information required in order to make  * suggestions.  */
end_comment

begin_class
DECL|class|ContextMapping
specifier|public
specifier|abstract
class|class
name|ContextMapping
implements|implements
name|ToXContent
block|{
comment|/** Character used to separate several contexts */
DECL|field|SEPARATOR
specifier|public
specifier|static
specifier|final
name|char
name|SEPARATOR
init|=
literal|'\u001D'
decl_stmt|;
comment|/** Dummy Context Mapping that should be used if no context is used*/
DECL|field|EMPTY_MAPPING
specifier|public
specifier|static
specifier|final
name|SortedMap
argument_list|<
name|String
argument_list|,
name|ContextMapping
argument_list|>
name|EMPTY_MAPPING
init|=
name|Maps
operator|.
name|newTreeMap
argument_list|()
decl_stmt|;
comment|/** Dummy Context Config matching the Dummy Mapping by providing an empty context*/
DECL|field|EMPTY_CONFIG
specifier|public
specifier|static
specifier|final
name|SortedMap
argument_list|<
name|String
argument_list|,
name|ContextConfig
argument_list|>
name|EMPTY_CONFIG
init|=
name|Maps
operator|.
name|newTreeMap
argument_list|()
decl_stmt|;
comment|/** Dummy Context matching the Dummy Mapping by not wrapping a {@link TokenStream} */
DECL|field|EMPTY_CONTEXT
specifier|public
specifier|static
specifier|final
name|Context
name|EMPTY_CONTEXT
init|=
operator|new
name|Context
argument_list|(
name|EMPTY_CONFIG
argument_list|,
literal|null
argument_list|)
decl_stmt|;
DECL|field|FIELD_VALUE
specifier|public
specifier|static
specifier|final
name|String
name|FIELD_VALUE
init|=
literal|"value"
decl_stmt|;
DECL|field|FIELD_MISSING
specifier|public
specifier|static
specifier|final
name|String
name|FIELD_MISSING
init|=
literal|"default"
decl_stmt|;
DECL|field|FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|FIELD_TYPE
init|=
literal|"type"
decl_stmt|;
DECL|field|type
specifier|protected
specifier|final
name|String
name|type
decl_stmt|;
comment|// Type of the Contextmapping
DECL|field|name
specifier|protected
specifier|final
name|String
name|name
decl_stmt|;
comment|/**      * Define a new context mapping of a specific type      *       * @param type      *            name of the new context mapping      */
DECL|method|ContextMapping
specifier|protected
name|ContextMapping
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/**      * @return the type name of the context      */
DECL|method|type
specifier|protected
name|String
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      * @return the name/id of the context      */
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|name
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
specifier|final
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FIELD_TYPE
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|toInnerXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
comment|/**      * A {@link ContextMapping} combined with the information provided by a document      * form a {@link ContextConfig} which is used to build the underlying FST.      *       * @param parseContext context of parsing phase       * @param parser {@link XContentParser} used to read and setup the configuration      * @return A {@link ContextConfig} related to<b>this</b> mapping      *       * @throws IOException      * @throws ElasticsearchParseException      */
DECL|method|parseContext
specifier|public
specifier|abstract
name|ContextConfig
name|parseContext
parameter_list|(
name|ParseContext
name|parseContext
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
throws|,
name|ElasticsearchParseException
function_decl|;
DECL|method|defaultConfig
specifier|public
specifier|abstract
name|ContextConfig
name|defaultConfig
parameter_list|()
function_decl|;
comment|/**      * Parse a query according to the context. Parsing starts at parsers<b>current</b> position      *       * @param name name of the context       * @param parser {@link XContentParser} providing the data of the query      *       * @return {@link ContextQuery} according to this mapping      *       * @throws IOException      * @throws ElasticsearchParseException      */
DECL|method|parseQuery
specifier|public
specifier|abstract
name|ContextQuery
name|parseQuery
parameter_list|(
name|String
name|name
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
throws|,
name|ElasticsearchParseException
function_decl|;
comment|/**      * Since every context mapping is assumed to have a name given by the field name of an context object, this      * method is used to build the value used to serialize the mapping      *       * @param builder builder to append the mapping to      * @param params parameters passed to the builder      *       * @return the builder used      *       * @throws IOException      */
DECL|method|toInnerXContent
specifier|protected
specifier|abstract
name|XContentBuilder
name|toInnerXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Test equality of two mapping      *       * @param thisMappings first mapping      * @param otherMappings second mapping      *       * @return true if both arguments are equal      */
DECL|method|mappingsAreEqual
specifier|public
specifier|static
name|boolean
name|mappingsAreEqual
parameter_list|(
name|SortedMap
argument_list|<
name|String
argument_list|,
name|?
extends|extends
name|ContextMapping
argument_list|>
name|thisMappings
parameter_list|,
name|SortedMap
argument_list|<
name|String
argument_list|,
name|?
extends|extends
name|ContextMapping
argument_list|>
name|otherMappings
parameter_list|)
block|{
return|return
name|Iterables
operator|.
name|elementsEqual
argument_list|(
name|thisMappings
operator|.
name|entrySet
argument_list|()
argument_list|,
name|otherMappings
operator|.
name|entrySet
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
try|try
block|{
return|return
name|toXContent
argument_list|(
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
operator|.
name|string
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
name|super
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
comment|/**      * A collection of {@link ContextMapping}s, their {@link ContextConfig}uration and a      * Document form a complete {@link Context}. Since this Object provides all information used      * to setup a suggestion, it can be used to wrap the entire {@link TokenStream} used to build a      * path within the {@link FST}.         */
DECL|class|Context
specifier|public
specifier|static
class|class
name|Context
block|{
DECL|field|contexts
specifier|final
name|SortedMap
argument_list|<
name|String
argument_list|,
name|ContextConfig
argument_list|>
name|contexts
decl_stmt|;
DECL|field|doc
specifier|final
name|Document
name|doc
decl_stmt|;
DECL|method|Context
specifier|public
name|Context
parameter_list|(
name|SortedMap
argument_list|<
name|String
argument_list|,
name|ContextConfig
argument_list|>
name|contexts
parameter_list|,
name|Document
name|doc
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|contexts
operator|=
name|contexts
expr_stmt|;
name|this
operator|.
name|doc
operator|=
name|doc
expr_stmt|;
block|}
comment|/**          * Wrap the {@link TokenStream} according to the provided informations of {@link ContextConfig}          * and a related {@link Document}.          *            * @param tokenStream {@link TokenStream} to wrap           *           * @return wrapped token stream          */
DECL|method|wrapTokenStream
specifier|public
name|TokenStream
name|wrapTokenStream
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|)
block|{
for|for
control|(
name|ContextConfig
name|context
range|:
name|contexts
operator|.
name|values
argument_list|()
control|)
block|{
name|tokenStream
operator|=
name|context
operator|.
name|wrapTokenStream
argument_list|(
name|doc
argument_list|,
name|tokenStream
argument_list|)
expr_stmt|;
block|}
return|return
name|tokenStream
return|;
block|}
block|}
comment|/**      *  A {@link ContextMapping} combined with the information provided by a document      *  form a {@link ContextConfig} which is used to build the underlying {@link FST}. This class hold      *  a simple method wrapping a {@link TokenStream} by provided document informations.      */
DECL|class|ContextConfig
specifier|public
specifier|static
specifier|abstract
class|class
name|ContextConfig
block|{
comment|/**          * Wrap a {@link TokenStream} for building suggestions to use context informations          * provided by a document or a {@link ContextMapping}          *            * @param doc document related to the stream          * @param stream original stream used to build the underlying {@link FST}          *           * @return A new {@link TokenStream} providing additional context information          */
DECL|method|wrapTokenStream
specifier|protected
specifier|abstract
name|TokenStream
name|wrapTokenStream
parameter_list|(
name|Document
name|doc
parameter_list|,
name|TokenStream
name|stream
parameter_list|)
function_decl|;
block|}
comment|/**      * A {@link ContextQuery} defines the context information for a specific {@link ContextMapping}      * defined within a suggestion request. According to the parameters set in the request and the      * {@link ContextMapping} such a query is used to wrap the {@link TokenStream} of the actual      * suggestion request into a {@link TokenStream} with the context settings      */
DECL|class|ContextQuery
specifier|public
specifier|static
specifier|abstract
class|class
name|ContextQuery
implements|implements
name|ToXContent
block|{
DECL|field|name
specifier|protected
specifier|final
name|String
name|name
decl_stmt|;
DECL|method|ContextQuery
specifier|protected
name|ContextQuery
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**          * Create a automaton for a given context query this automaton will be used          * to find the matching paths with the fst          *           * @param preserveSep set an additional char (<code>XAnalyzingSuggester.SEP_LABEL</code>) between each context query          * @param queries list of {@link ContextQuery} defining the lookup context          *           * @return Automaton matching the given Query          */
DECL|method|toAutomaton
specifier|public
specifier|static
name|Automaton
name|toAutomaton
parameter_list|(
name|boolean
name|preserveSep
parameter_list|,
name|Iterable
argument_list|<
name|ContextQuery
argument_list|>
name|queries
parameter_list|)
block|{
name|Automaton
name|a
init|=
name|Automata
operator|.
name|makeEmptyString
argument_list|()
decl_stmt|;
name|Automaton
name|gap
init|=
name|Automata
operator|.
name|makeChar
argument_list|(
name|ContextMapping
operator|.
name|SEPARATOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|preserveSep
condition|)
block|{
comment|// if separators are preserved the fst contains a SEP_LABEL
comment|// behind each gap. To have a matching automaton, we need to
comment|// include the SEP_LABEL in the query as well
name|gap
operator|=
name|Operations
operator|.
name|concatenate
argument_list|(
name|gap
argument_list|,
name|Automata
operator|.
name|makeChar
argument_list|(
name|XAnalyzingSuggester
operator|.
name|SEP_LABEL
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ContextQuery
name|query
range|:
name|queries
control|)
block|{
name|a
operator|=
name|Operations
operator|.
name|concatenate
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|query
operator|.
name|toAutomaton
argument_list|()
argument_list|,
name|gap
argument_list|,
name|a
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|Operations
operator|.
name|determinize
argument_list|(
name|a
argument_list|)
return|;
block|}
comment|/**          * Build a LookUp Automaton for this context.          * @return LookUp Automaton          */
DECL|method|toAutomaton
specifier|protected
specifier|abstract
name|Automaton
name|toAutomaton
parameter_list|()
function_decl|;
comment|/**          * Parse a set of {@link ContextQuery} according to a given mapping           * @param mappings List of mapping defined y the suggest field          * @param parser parser holding the settings of the queries. The parsers          *        current token is assumed hold an array. The number of elements          *        in this array must match the number of elements in the mappings.             * @return List of context queries          *           * @throws IOException if something unexpected happened on the underlying stream          * @throws ElasticsearchParseException if the list of queries could not be parsed          */
DECL|method|parseQueries
specifier|public
specifier|static
name|List
argument_list|<
name|ContextQuery
argument_list|>
name|parseQueries
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|ContextMapping
argument_list|>
name|mappings
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
throws|,
name|ElasticsearchParseException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|ContextQuery
argument_list|>
name|querySet
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Token
name|token
init|=
name|parser
operator|.
name|currentToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
name|String
name|name
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
name|ContextMapping
name|mapping
init|=
name|mappings
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapping
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"no mapping defined for ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|querySet
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|mapping
operator|.
name|parseQuery
argument_list|(
name|name
argument_list|,
name|parser
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|ContextQuery
argument_list|>
name|queries
init|=
name|Lists
operator|.
name|newArrayListWithExpectedSize
argument_list|(
name|mappings
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ContextMapping
name|mapping
range|:
name|mappings
operator|.
name|values
argument_list|()
control|)
block|{
name|queries
operator|.
name|add
argument_list|(
name|querySet
operator|.
name|get
argument_list|(
name|mapping
operator|.
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|queries
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
try|try
block|{
return|return
name|toXContent
argument_list|(
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
operator|.
name|string
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
name|super
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

