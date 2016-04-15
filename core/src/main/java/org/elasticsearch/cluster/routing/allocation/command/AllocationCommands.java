begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation.command
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|command
package|;
end_package

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
name|action
operator|.
name|support
operator|.
name|ToXContentToBytes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|RoutingAllocation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|RoutingExplanations
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|ArrayList
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_comment
comment|/**  * A simple {@link AllocationCommand} composite managing several  * {@link AllocationCommand} implementations  */
end_comment

begin_class
DECL|class|AllocationCommands
specifier|public
class|class
name|AllocationCommands
extends|extends
name|ToXContentToBytes
block|{
DECL|field|commands
specifier|private
specifier|final
name|List
argument_list|<
name|AllocationCommand
argument_list|>
name|commands
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**      * Creates a new set of {@link AllocationCommands}      *      * @param commands {@link AllocationCommand}s that are wrapped by this instance      */
DECL|method|AllocationCommands
specifier|public
name|AllocationCommands
parameter_list|(
name|AllocationCommand
modifier|...
name|commands
parameter_list|)
block|{
if|if
condition|(
name|commands
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|commands
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|commands
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Adds a set of commands to this collection      * @param commands Array of commands to add to this instance      * @return {@link AllocationCommands} with the given commands added      */
DECL|method|add
specifier|public
name|AllocationCommands
name|add
parameter_list|(
name|AllocationCommand
modifier|...
name|commands
parameter_list|)
block|{
if|if
condition|(
name|commands
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|commands
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|commands
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Get the commands wrapped by this instance      * @return {@link List} of commands      */
DECL|method|commands
specifier|public
name|List
argument_list|<
name|AllocationCommand
argument_list|>
name|commands
parameter_list|()
block|{
return|return
name|this
operator|.
name|commands
return|;
block|}
comment|/**      * Executes all wrapped commands on a given {@link RoutingAllocation}      * @param allocation {@link RoutingAllocation} to apply this command to      * @throws org.elasticsearch.ElasticsearchException if something happens during execution      */
DECL|method|execute
specifier|public
name|RoutingExplanations
name|execute
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|,
name|boolean
name|explain
parameter_list|)
block|{
name|RoutingExplanations
name|explanations
init|=
operator|new
name|RoutingExplanations
argument_list|()
decl_stmt|;
for|for
control|(
name|AllocationCommand
name|command
range|:
name|commands
control|)
block|{
name|explanations
operator|.
name|add
argument_list|(
name|command
operator|.
name|execute
argument_list|(
name|allocation
argument_list|,
name|explain
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|explanations
return|;
block|}
comment|/**      * Reads a {@link AllocationCommands} from a {@link StreamInput}      * @param in {@link StreamInput} to read from      * @return {@link AllocationCommands} read      *      * @throws IOException if something happens during read      */
DECL|method|readFrom
specifier|public
specifier|static
name|AllocationCommands
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|AllocationCommands
name|commands
init|=
operator|new
name|AllocationCommands
argument_list|()
decl_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|commands
operator|.
name|add
argument_list|(
name|in
operator|.
name|readNamedWriteable
argument_list|(
name|AllocationCommand
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|commands
return|;
block|}
comment|/**      * Writes {@link AllocationCommands} to a {@link StreamOutput}      *      * @param commands Commands to write      * @param out {@link StreamOutput} to write the commands to      * @throws IOException if something happens during write      */
DECL|method|writeTo
specifier|public
specifier|static
name|void
name|writeTo
parameter_list|(
name|AllocationCommands
name|commands
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|commands
operator|.
name|commands
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|AllocationCommand
name|command
range|:
name|commands
operator|.
name|commands
control|)
block|{
name|out
operator|.
name|writeNamedWriteable
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Reads {@link AllocationCommands} from a {@link XContentParser}      *<pre>      *     {      *         "commands" : [      *              {"allocate" : {"index" : "test", "shard" : 0, "node" : "test"}}      *         ]      *     }      *</pre>      * @param parser {@link XContentParser} to read the commands from      * @param registry of allocation command parsers      * @return {@link AllocationCommands} read      * @throws IOException if something bad happens while reading the stream      */
DECL|method|fromXContent
specifier|public
specifier|static
name|AllocationCommands
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|,
name|AllocationCommandRegistry
name|registry
parameter_list|)
throws|throws
name|IOException
block|{
name|AllocationCommands
name|commands
init|=
operator|new
name|AllocationCommands
argument_list|()
decl_stmt|;
name|XContentParser
operator|.
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
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"No commands"
argument_list|)
throw|;
block|}
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
if|if
condition|(
operator|!
name|parser
operator|.
name|currentName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"commands"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"expected field name to be named [commands], got [{}] instead"
argument_list|,
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|parser
operator|.
name|currentName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"commands"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"expected field name to be named [commands], got [{}] instead"
argument_list|,
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
throw|;
block|}
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"commands should follow with an array element"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
comment|// ok...
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"expected either field name [commands], or start array, got [{}] instead"
argument_list|,
name|token
argument_list|)
throw|;
block|}
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
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
comment|// move to the command name
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|String
name|commandName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|commands
operator|.
name|add
argument_list|(
name|registry
operator|.
name|lookup
argument_list|(
name|commandName
argument_list|,
name|parseFieldMatcher
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
argument_list|)
expr_stmt|;
comment|// move to the end object one
if|if
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"allocation command is malformed, done parsing a command, but didn't get END_OBJECT, got [{}] instead"
argument_list|,
name|token
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"allocation command is malformed, got [{}] instead"
argument_list|,
name|token
argument_list|)
throw|;
block|}
block|}
return|return
name|commands
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
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
name|startArray
argument_list|(
literal|"commands"
argument_list|)
expr_stmt|;
for|for
control|(
name|AllocationCommand
name|command
range|:
name|commands
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|command
operator|.
name|name
argument_list|()
argument_list|,
name|command
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|AllocationCommands
name|other
init|=
operator|(
name|AllocationCommands
operator|)
name|obj
decl_stmt|;
comment|// Override equals and hashCode for testing
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|commands
argument_list|,
name|other
operator|.
name|commands
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
comment|// Override equals and hashCode for testing
return|return
name|Objects
operator|.
name|hashCode
argument_list|(
name|commands
argument_list|)
return|;
block|}
block|}
end_class

end_unit

