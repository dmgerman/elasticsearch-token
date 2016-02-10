begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.phrase
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|phrase
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
name|io
operator|.
name|stream
operator|.
name|NamedWriteable
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
name|index
operator|.
name|query
operator|.
name|QueryParseContext
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
name|WordScorer
operator|.
name|WordScorerFactory
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

begin_class
DECL|class|SmoothingModel
specifier|public
specifier|abstract
class|class
name|SmoothingModel
implements|implements
name|NamedWriteable
argument_list|<
name|SmoothingModel
argument_list|>
implements|,
name|ToXContent
block|{
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
argument_list|(
name|getWriteableName
argument_list|()
argument_list|)
expr_stmt|;
name|innerToXContent
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
annotation|@
name|Override
DECL|method|equals
specifier|public
specifier|final
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
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
name|SmoothingModel
name|other
init|=
operator|(
name|SmoothingModel
operator|)
name|obj
decl_stmt|;
return|return
name|doEquals
argument_list|(
name|other
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
specifier|final
name|int
name|hashCode
parameter_list|()
block|{
comment|/*          * Override hashCode here and forward to an abstract method to force          * extensions of this class to override hashCode in the same way that we          * force them to override equals. This also prevents false positives in          * CheckStyle's EqualsHashCode check.          */
return|return
name|doHashCode
argument_list|()
return|;
block|}
DECL|method|doHashCode
specifier|protected
specifier|abstract
name|int
name|doHashCode
parameter_list|()
function_decl|;
DECL|method|fromXContent
specifier|public
specifier|static
name|SmoothingModel
name|fromXContent
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|ParseFieldMatcher
name|parseFieldMatcher
init|=
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|SmoothingModel
name|model
init|=
literal|null
decl_stmt|;
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
name|END_OBJECT
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
name|FIELD_NAME
condition|)
block|{
name|fieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
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
name|START_OBJECT
condition|)
block|{
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|fieldName
argument_list|,
name|LinearInterpolation
operator|.
name|PARSE_FIELD
argument_list|)
condition|)
block|{
name|model
operator|=
name|LinearInterpolation
operator|.
name|PROTOTYPE
operator|.
name|innerFromXContent
argument_list|(
name|parseContext
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|fieldName
argument_list|,
name|Laplace
operator|.
name|PARSE_FIELD
argument_list|)
condition|)
block|{
name|model
operator|=
name|Laplace
operator|.
name|PROTOTYPE
operator|.
name|innerFromXContent
argument_list|(
name|parseContext
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|fieldName
argument_list|,
name|StupidBackoff
operator|.
name|PARSE_FIELD
argument_list|)
condition|)
block|{
name|model
operator|=
name|StupidBackoff
operator|.
name|PROTOTYPE
operator|.
name|innerFromXContent
argument_list|(
name|parseContext
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"suggester[phrase] doesn't support object field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"[smoothing] unknown token ["
operator|+
name|token
operator|+
literal|"] after ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
return|return
name|model
return|;
block|}
DECL|method|innerFromXContent
specifier|public
specifier|abstract
name|SmoothingModel
name|innerFromXContent
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|buildWordScorerFactory
specifier|public
specifier|abstract
name|WordScorerFactory
name|buildWordScorerFactory
parameter_list|()
function_decl|;
comment|/**      * subtype specific implementation of "equals".      */
DECL|method|doEquals
specifier|protected
specifier|abstract
name|boolean
name|doEquals
parameter_list|(
name|SmoothingModel
name|other
parameter_list|)
function_decl|;
DECL|method|innerToXContent
specifier|protected
specifier|abstract
name|XContentBuilder
name|innerToXContent
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
block|}
end_class

end_unit

