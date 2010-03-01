begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.json
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|json
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|json
operator|.
name|JsonBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|trove
operator|.
name|ExtTObjectFloatHashMap
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
name|List
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|QueryStringJsonQueryBuilder
specifier|public
class|class
name|QueryStringJsonQueryBuilder
extends|extends
name|BaseJsonQueryBuilder
block|{
DECL|enum|Operator
specifier|public
specifier|static
enum|enum
name|Operator
block|{
DECL|enum constant|OR
name|OR
block|,
DECL|enum constant|AND
name|AND
block|}
DECL|field|queryString
specifier|private
specifier|final
name|String
name|queryString
decl_stmt|;
DECL|field|defaultField
specifier|private
name|String
name|defaultField
decl_stmt|;
DECL|field|defaultOperator
specifier|private
name|Operator
name|defaultOperator
decl_stmt|;
DECL|field|analyzer
specifier|private
name|String
name|analyzer
decl_stmt|;
DECL|field|allowLeadingWildcard
specifier|private
name|Boolean
name|allowLeadingWildcard
decl_stmt|;
DECL|field|lowercaseExpandedTerms
specifier|private
name|Boolean
name|lowercaseExpandedTerms
decl_stmt|;
DECL|field|enablePositionIncrements
specifier|private
name|Boolean
name|enablePositionIncrements
decl_stmt|;
DECL|field|fuzzyMinSim
specifier|private
name|float
name|fuzzyMinSim
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|boost
specifier|private
name|float
name|boost
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|fuzzyPrefixLength
specifier|private
name|int
name|fuzzyPrefixLength
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|phraseSlop
specifier|private
name|int
name|phraseSlop
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|fields
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|fields
decl_stmt|;
DECL|field|fieldsBoosts
specifier|private
name|ExtTObjectFloatHashMap
argument_list|<
name|String
argument_list|>
name|fieldsBoosts
decl_stmt|;
DECL|field|useDisMax
specifier|private
name|Boolean
name|useDisMax
decl_stmt|;
DECL|field|tieBreaker
specifier|private
name|float
name|tieBreaker
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|QueryStringJsonQueryBuilder
specifier|public
name|QueryStringJsonQueryBuilder
parameter_list|(
name|String
name|queryString
parameter_list|)
block|{
name|this
operator|.
name|queryString
operator|=
name|queryString
expr_stmt|;
block|}
DECL|method|defaultField
specifier|public
name|QueryStringJsonQueryBuilder
name|defaultField
parameter_list|(
name|String
name|defaultField
parameter_list|)
block|{
name|this
operator|.
name|defaultField
operator|=
name|defaultField
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a field to run the query string against.      */
DECL|method|field
specifier|public
name|QueryStringJsonQueryBuilder
name|field
parameter_list|(
name|String
name|field
parameter_list|)
block|{
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|fields
operator|=
name|newArrayList
argument_list|()
expr_stmt|;
block|}
name|fields
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a field to run the query string against with a specific boost.      */
DECL|method|field
specifier|public
name|QueryStringJsonQueryBuilder
name|field
parameter_list|(
name|String
name|field
parameter_list|,
name|float
name|boost
parameter_list|)
block|{
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|fields
operator|=
name|newArrayList
argument_list|()
expr_stmt|;
block|}
name|fields
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
if|if
condition|(
name|fieldsBoosts
operator|==
literal|null
condition|)
block|{
name|fieldsBoosts
operator|=
operator|new
name|ExtTObjectFloatHashMap
argument_list|<
name|String
argument_list|>
argument_list|()
operator|.
name|defaultReturnValue
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|fieldsBoosts
operator|.
name|put
argument_list|(
name|field
argument_list|,
name|boost
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * When more than one field is used with the query string, should queries be combined using      * dis max, or boolean query. Defaults to dis max (<tt>true</tt>).      */
DECL|method|useDisMax
specifier|public
name|QueryStringJsonQueryBuilder
name|useDisMax
parameter_list|(
name|boolean
name|useDisMax
parameter_list|)
block|{
name|this
operator|.
name|useDisMax
operator|=
name|useDisMax
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * When more than one field is used with the query string, and combined queries are using      * dis max, control the tie breaker for it.      */
DECL|method|tieBreaker
specifier|public
name|QueryStringJsonQueryBuilder
name|tieBreaker
parameter_list|(
name|float
name|tieBreaker
parameter_list|)
block|{
name|this
operator|.
name|tieBreaker
operator|=
name|tieBreaker
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|defaultOperator
specifier|public
name|QueryStringJsonQueryBuilder
name|defaultOperator
parameter_list|(
name|Operator
name|defaultOperator
parameter_list|)
block|{
name|this
operator|.
name|defaultOperator
operator|=
name|defaultOperator
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|analyzer
specifier|public
name|QueryStringJsonQueryBuilder
name|analyzer
parameter_list|(
name|String
name|analyzer
parameter_list|)
block|{
name|this
operator|.
name|analyzer
operator|=
name|analyzer
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|allowLeadingWildcard
specifier|public
name|QueryStringJsonQueryBuilder
name|allowLeadingWildcard
parameter_list|(
name|boolean
name|allowLeadingWildcard
parameter_list|)
block|{
name|this
operator|.
name|allowLeadingWildcard
operator|=
name|allowLeadingWildcard
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|lowercaseExpandedTerms
specifier|public
name|QueryStringJsonQueryBuilder
name|lowercaseExpandedTerms
parameter_list|(
name|boolean
name|lowercaseExpandedTerms
parameter_list|)
block|{
name|this
operator|.
name|lowercaseExpandedTerms
operator|=
name|lowercaseExpandedTerms
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|enablePositionIncrements
specifier|public
name|QueryStringJsonQueryBuilder
name|enablePositionIncrements
parameter_list|(
name|boolean
name|enablePositionIncrements
parameter_list|)
block|{
name|this
operator|.
name|enablePositionIncrements
operator|=
name|enablePositionIncrements
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|fuzzyMinSim
specifier|public
name|QueryStringJsonQueryBuilder
name|fuzzyMinSim
parameter_list|(
name|float
name|fuzzyMinSim
parameter_list|)
block|{
name|this
operator|.
name|fuzzyMinSim
operator|=
name|fuzzyMinSim
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|boost
specifier|public
name|QueryStringJsonQueryBuilder
name|boost
parameter_list|(
name|float
name|boost
parameter_list|)
block|{
name|this
operator|.
name|boost
operator|=
name|boost
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|fuzzyPrefixLength
specifier|public
name|QueryStringJsonQueryBuilder
name|fuzzyPrefixLength
parameter_list|(
name|int
name|fuzzyPrefixLength
parameter_list|)
block|{
name|this
operator|.
name|fuzzyPrefixLength
operator|=
name|fuzzyPrefixLength
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|phraseSlop
specifier|public
name|QueryStringJsonQueryBuilder
name|phraseSlop
parameter_list|(
name|int
name|phraseSlop
parameter_list|)
block|{
name|this
operator|.
name|phraseSlop
operator|=
name|phraseSlop
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|doJson
annotation|@
name|Override
specifier|protected
name|void
name|doJson
parameter_list|(
name|JsonBuilder
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
name|QueryStringJsonQueryParser
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"query"
argument_list|,
name|queryString
argument_list|)
expr_stmt|;
if|if
condition|(
name|defaultField
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"defaultField"
argument_list|,
name|defaultField
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fields
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"fields"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|float
name|boost
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|fieldsBoosts
operator|!=
literal|null
condition|)
block|{
name|boost
operator|=
name|fieldsBoosts
operator|.
name|get
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|boost
operator|!=
operator|-
literal|1
condition|)
block|{
name|field
operator|+=
literal|"^"
operator|+
name|boost
expr_stmt|;
block|}
name|builder
operator|.
name|string
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|useDisMax
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"useDisMax"
argument_list|,
name|useDisMax
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tieBreaker
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"tieBreaker"
argument_list|,
name|tieBreaker
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|defaultOperator
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"defaultOperator"
argument_list|,
name|defaultOperator
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|analyzer
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
name|analyzer
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|allowLeadingWildcard
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"allowLeadingWildcard"
argument_list|,
name|allowLeadingWildcard
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lowercaseExpandedTerms
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"lowercaseExpandedTerms"
argument_list|,
name|lowercaseExpandedTerms
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|enablePositionIncrements
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"enablePositionIncrements"
argument_list|,
name|enablePositionIncrements
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fuzzyMinSim
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"fuzzyMinSim"
argument_list|,
name|fuzzyMinSim
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|boost
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"boost"
argument_list|,
name|boost
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fuzzyPrefixLength
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"fuzzyPrefixLength"
argument_list|,
name|fuzzyPrefixLength
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|phraseSlop
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"phraseSlop"
argument_list|,
name|phraseSlop
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

