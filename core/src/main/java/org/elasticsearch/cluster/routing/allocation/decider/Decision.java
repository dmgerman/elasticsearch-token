begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation.decider
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
name|decider
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_comment
comment|/**  * This abstract class defining basic {@link Decision} used during shard  * allocation process.  *   * @see AllocationDecider  */
end_comment

begin_class
DECL|class|Decision
specifier|public
specifier|abstract
class|class
name|Decision
implements|implements
name|ToXContent
block|{
DECL|field|ALWAYS
specifier|public
specifier|static
specifier|final
name|Decision
name|ALWAYS
init|=
operator|new
name|Single
argument_list|(
name|Type
operator|.
name|YES
argument_list|)
decl_stmt|;
DECL|field|YES
specifier|public
specifier|static
specifier|final
name|Decision
name|YES
init|=
operator|new
name|Single
argument_list|(
name|Type
operator|.
name|YES
argument_list|)
decl_stmt|;
DECL|field|NO
specifier|public
specifier|static
specifier|final
name|Decision
name|NO
init|=
operator|new
name|Single
argument_list|(
name|Type
operator|.
name|NO
argument_list|)
decl_stmt|;
DECL|field|THROTTLE
specifier|public
specifier|static
specifier|final
name|Decision
name|THROTTLE
init|=
operator|new
name|Single
argument_list|(
name|Type
operator|.
name|THROTTLE
argument_list|)
decl_stmt|;
comment|/**      * Creates a simple decision       * @param type {@link Type} of the decision      * @param label label for the Decider that produced this decision      * @param explanation explanation of the decision      * @param explanationParams additional parameters for the decision      * @return new {@link Decision} instance      */
DECL|method|single
specifier|public
specifier|static
name|Decision
name|single
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|label
parameter_list|,
name|String
name|explanation
parameter_list|,
name|Object
modifier|...
name|explanationParams
parameter_list|)
block|{
return|return
operator|new
name|Single
argument_list|(
name|type
argument_list|,
name|label
argument_list|,
name|explanation
argument_list|,
name|explanationParams
argument_list|)
return|;
block|}
DECL|method|writeTo
specifier|public
specifier|static
name|void
name|writeTo
parameter_list|(
name|Decision
name|decision
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|decision
operator|instanceof
name|Multi
condition|)
block|{
comment|// Flag specifying whether it is a Multi or Single Decision
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
operator|(
operator|(
name|Multi
operator|)
name|decision
operator|)
operator|.
name|decisions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Decision
name|d
range|:
operator|(
operator|(
name|Multi
operator|)
name|decision
operator|)
operator|.
name|decisions
control|)
block|{
name|writeTo
argument_list|(
name|d
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Flag specifying whether it is a Multi or Single Decision
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Single
name|d
init|=
operator|(
operator|(
name|Single
operator|)
name|decision
operator|)
decl_stmt|;
name|Type
operator|.
name|writeTo
argument_list|(
name|d
operator|.
name|type
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|d
operator|.
name|label
argument_list|)
expr_stmt|;
comment|// Flatten explanation on serialization, so that explanationParams
comment|// do not need to be serialized
name|out
operator|.
name|writeOptionalString
argument_list|(
name|d
operator|.
name|getExplanation
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|readFrom
specifier|public
specifier|static
name|Decision
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Determine whether to read a Single or Multi Decision
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|Multi
name|result
init|=
operator|new
name|Multi
argument_list|()
decl_stmt|;
name|int
name|decisionCount
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
name|decisionCount
condition|;
name|i
operator|++
control|)
block|{
name|Decision
name|s
init|=
name|readFrom
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|result
operator|.
name|decisions
operator|.
name|add
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
else|else
block|{
name|Single
name|result
init|=
operator|new
name|Single
argument_list|()
decl_stmt|;
name|result
operator|.
name|type
operator|=
name|Type
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|result
operator|.
name|label
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|result
operator|.
name|explanationString
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
comment|/**      * This enumeration defines the       * possible types of decisions       */
DECL|enum|Type
specifier|public
specifier|static
enum|enum
name|Type
block|{
DECL|enum constant|YES
name|YES
block|,
DECL|enum constant|NO
name|NO
block|,
DECL|enum constant|THROTTLE
name|THROTTLE
block|;
DECL|method|resolve
specifier|public
specifier|static
name|Type
name|resolve
parameter_list|(
name|String
name|s
parameter_list|)
block|{
return|return
name|Type
operator|.
name|valueOf
argument_list|(
name|s
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
return|;
block|}
DECL|method|readFrom
specifier|public
specifier|static
name|Type
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|i
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|i
condition|)
block|{
case|case
literal|0
case|:
return|return
name|NO
return|;
case|case
literal|1
case|:
return|return
name|YES
return|;
case|case
literal|2
case|:
return|return
name|THROTTLE
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No Type for integer ["
operator|+
name|i
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
DECL|method|writeTo
specifier|public
specifier|static
name|void
name|writeTo
parameter_list|(
name|Type
name|type
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|NO
case|:
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
break|break;
case|case
name|YES
case|:
name|out
operator|.
name|writeVInt
argument_list|(
literal|1
argument_list|)
expr_stmt|;
break|break;
case|case
name|THROTTLE
case|:
name|out
operator|.
name|writeVInt
argument_list|(
literal|2
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid Type ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**      * Get the {@link Type} of this decision      * @return {@link Type} of this decision      */
DECL|method|type
specifier|public
specifier|abstract
name|Type
name|type
parameter_list|()
function_decl|;
DECL|method|label
specifier|public
specifier|abstract
name|String
name|label
parameter_list|()
function_decl|;
comment|/**      * Simple class representing a single decision      */
DECL|class|Single
specifier|public
specifier|static
class|class
name|Single
extends|extends
name|Decision
block|{
DECL|field|type
specifier|private
name|Type
name|type
decl_stmt|;
DECL|field|label
specifier|private
name|String
name|label
decl_stmt|;
DECL|field|explanation
specifier|private
name|String
name|explanation
decl_stmt|;
DECL|field|explanationString
specifier|private
name|String
name|explanationString
decl_stmt|;
DECL|field|explanationParams
specifier|private
name|Object
index|[]
name|explanationParams
decl_stmt|;
DECL|method|Single
specifier|public
name|Single
parameter_list|()
block|{          }
comment|/**          * Creates a new {@link Single} decision of a given type           * @param type {@link Type} of the decision          */
DECL|method|Single
specifier|public
name|Single
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
name|this
argument_list|(
name|type
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|(
name|Object
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**          * Creates a new {@link Single} decision of a given type          *            * @param type {@link Type} of the decision          * @param explanation An explanation of this {@link Decision}          * @param explanationParams A set of additional parameters          */
DECL|method|Single
specifier|public
name|Single
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|label
parameter_list|,
name|String
name|explanation
parameter_list|,
name|Object
modifier|...
name|explanationParams
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|label
operator|=
name|label
expr_stmt|;
name|this
operator|.
name|explanation
operator|=
name|explanation
expr_stmt|;
name|this
operator|.
name|explanationParams
operator|=
name|explanationParams
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|Type
name|type
parameter_list|()
block|{
return|return
name|this
operator|.
name|type
return|;
block|}
annotation|@
name|Override
DECL|method|label
specifier|public
name|String
name|label
parameter_list|()
block|{
return|return
name|this
operator|.
name|label
return|;
block|}
comment|/**          * Returns the explanation string, fully formatted. Only formats the string once          */
DECL|method|getExplanation
specifier|public
name|String
name|getExplanation
parameter_list|()
block|{
if|if
condition|(
name|explanationString
operator|==
literal|null
operator|&&
name|explanation
operator|!=
literal|null
condition|)
block|{
name|explanationString
operator|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
name|explanation
argument_list|,
name|explanationParams
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|explanationString
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
if|if
condition|(
name|explanation
operator|==
literal|null
condition|)
block|{
return|return
name|type
operator|+
literal|"()"
return|;
block|}
return|return
name|type
operator|+
literal|"("
operator|+
name|getExplanation
argument_list|()
operator|+
literal|")"
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
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"decider"
argument_list|,
name|label
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"decision"
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|String
name|explanation
init|=
name|getExplanation
argument_list|()
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"explanation"
argument_list|,
name|explanation
operator|!=
literal|null
condition|?
name|explanation
else|:
literal|"none"
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
block|}
comment|/**      * Simple class representing a list of decisions      */
DECL|class|Multi
specifier|public
specifier|static
class|class
name|Multi
extends|extends
name|Decision
block|{
DECL|field|decisions
specifier|private
specifier|final
name|List
argument_list|<
name|Decision
argument_list|>
name|decisions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**          * Add a decision to this {@link Multi}decision instance          * @param decision {@link Decision} to add          * @return {@link Multi}decision instance with the given decision added          */
DECL|method|add
specifier|public
name|Multi
name|add
parameter_list|(
name|Decision
name|decision
parameter_list|)
block|{
name|decisions
operator|.
name|add
argument_list|(
name|decision
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|Type
name|type
parameter_list|()
block|{
name|Type
name|ret
init|=
name|Type
operator|.
name|YES
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
name|decisions
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Type
name|type
init|=
name|decisions
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|type
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|NO
condition|)
block|{
return|return
name|type
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|THROTTLE
condition|)
block|{
name|ret
operator|=
name|type
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
DECL|method|label
specifier|public
name|String
name|label
parameter_list|()
block|{
comment|// Multi decisions have no labels
return|return
literal|null
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
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Decision
name|decision
range|:
name|decisions
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
operator|.
name|append
argument_list|(
name|decision
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
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
literal|"decisions"
argument_list|)
expr_stmt|;
for|for
control|(
name|Decision
name|d
range|:
name|decisions
control|)
block|{
name|d
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
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
block|}
block|}
end_class

end_unit

