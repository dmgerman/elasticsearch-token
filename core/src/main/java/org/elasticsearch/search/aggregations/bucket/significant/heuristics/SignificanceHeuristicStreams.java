begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.significant.heuristics
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|significant
operator|.
name|heuristics
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|Map
import|;
end_import

begin_comment
comment|/**  * A registry for all significance heuristics. This is needed for reading them from a stream without knowing which  * one it is.  */
end_comment

begin_class
DECL|class|SignificanceHeuristicStreams
specifier|public
class|class
name|SignificanceHeuristicStreams
block|{
DECL|field|STREAMS
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Stream
argument_list|>
name|STREAMS
init|=
name|Collections
operator|.
name|emptyMap
argument_list|()
decl_stmt|;
static|static
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|Stream
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
name|JLHScore
operator|.
name|STREAM
operator|.
name|getName
argument_list|()
argument_list|,
name|JLHScore
operator|.
name|STREAM
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|PercentageScore
operator|.
name|STREAM
operator|.
name|getName
argument_list|()
argument_list|,
name|PercentageScore
operator|.
name|STREAM
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|MutualInformation
operator|.
name|STREAM
operator|.
name|getName
argument_list|()
argument_list|,
name|MutualInformation
operator|.
name|STREAM
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|GND
operator|.
name|STREAM
operator|.
name|getName
argument_list|()
argument_list|,
name|GND
operator|.
name|STREAM
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|ChiSquare
operator|.
name|STREAM
operator|.
name|getName
argument_list|()
argument_list|,
name|ChiSquare
operator|.
name|STREAM
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|ScriptHeuristic
operator|.
name|STREAM
operator|.
name|getName
argument_list|()
argument_list|,
name|ScriptHeuristic
operator|.
name|STREAM
argument_list|)
expr_stmt|;
name|STREAMS
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|map
argument_list|)
expr_stmt|;
block|}
DECL|method|read
specifier|public
specifier|static
name|SignificanceHeuristic
name|read
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|stream
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
operator|.
name|readResult
argument_list|(
name|in
argument_list|)
return|;
block|}
comment|/**      * A stream that knows how to read an heuristic from the input.      */
DECL|interface|Stream
specifier|public
specifier|static
interface|interface
name|Stream
block|{
DECL|method|readResult
name|SignificanceHeuristic
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|getName
name|String
name|getName
parameter_list|()
function_decl|;
block|}
comment|/**      * Registers the given stream and associate it with the given types.      *      * @param stream The stream to register      */
DECL|method|registerStream
specifier|public
specifier|static
specifier|synchronized
name|void
name|registerStream
parameter_list|(
name|Stream
name|stream
parameter_list|)
block|{
if|if
condition|(
name|STREAMS
operator|.
name|containsKey
argument_list|(
name|stream
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't register stream with name ["
operator|+
name|stream
operator|.
name|getName
argument_list|()
operator|+
literal|"] more than once"
argument_list|)
throw|;
block|}
name|HashMap
argument_list|<
name|String
argument_list|,
name|Stream
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
name|putAll
argument_list|(
name|STREAMS
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|stream
operator|.
name|getName
argument_list|()
argument_list|,
name|stream
argument_list|)
expr_stmt|;
name|STREAMS
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|map
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the stream that is registered for the given name      *      * @param name The given name      * @return The associated stream      */
DECL|method|stream
specifier|private
specifier|static
specifier|synchronized
name|Stream
name|stream
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|STREAMS
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
block|}
end_class

end_unit

