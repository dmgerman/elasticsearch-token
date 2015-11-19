begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent.support.filtering
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|support
operator|.
name|filtering
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
name|regex
operator|.
name|Regex
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
name|CollectionUtils
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

begin_class
DECL|class|FilterPath
specifier|public
class|class
name|FilterPath
block|{
DECL|field|EMPTY
specifier|static
specifier|final
name|FilterPath
name|EMPTY
init|=
operator|new
name|FilterPath
argument_list|()
decl_stmt|;
DECL|field|filter
specifier|private
specifier|final
name|String
name|filter
decl_stmt|;
DECL|field|segment
specifier|private
specifier|final
name|String
name|segment
decl_stmt|;
DECL|field|next
specifier|private
specifier|final
name|FilterPath
name|next
decl_stmt|;
DECL|field|simpleWildcard
specifier|private
specifier|final
name|boolean
name|simpleWildcard
decl_stmt|;
DECL|field|doubleWildcard
specifier|private
specifier|final
name|boolean
name|doubleWildcard
decl_stmt|;
DECL|method|FilterPath
specifier|protected
name|FilterPath
parameter_list|(
name|String
name|filter
parameter_list|,
name|String
name|segment
parameter_list|,
name|FilterPath
name|next
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
name|this
operator|.
name|segment
operator|=
name|segment
expr_stmt|;
name|this
operator|.
name|next
operator|=
name|next
expr_stmt|;
name|this
operator|.
name|simpleWildcard
operator|=
operator|(
name|segment
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|segment
operator|.
name|length
argument_list|()
operator|==
literal|1
operator|)
operator|&&
operator|(
name|segment
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'*'
operator|)
expr_stmt|;
name|this
operator|.
name|doubleWildcard
operator|=
operator|(
name|segment
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|segment
operator|.
name|length
argument_list|()
operator|==
literal|2
operator|)
operator|&&
operator|(
name|segment
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'*'
operator|)
operator|&&
operator|(
name|segment
operator|.
name|charAt
argument_list|(
literal|1
argument_list|)
operator|==
literal|'*'
operator|)
expr_stmt|;
block|}
DECL|method|FilterPath
specifier|private
name|FilterPath
parameter_list|()
block|{
name|this
argument_list|(
literal|"<empty>"
argument_list|,
literal|""
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|matchProperty
specifier|public
name|FilterPath
name|matchProperty
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
operator|(
name|next
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|simpleWildcard
operator|||
name|doubleWildcard
operator|||
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|segment
argument_list|,
name|name
argument_list|)
operator|)
condition|)
block|{
return|return
name|next
return|;
block|}
return|return
literal|null
return|;
block|}
DECL|method|matches
specifier|public
name|boolean
name|matches
parameter_list|()
block|{
return|return
name|next
operator|==
literal|null
return|;
block|}
DECL|method|isDoubleWildcard
name|boolean
name|isDoubleWildcard
parameter_list|()
block|{
return|return
name|doubleWildcard
return|;
block|}
DECL|method|isSimpleWildcard
name|boolean
name|isSimpleWildcard
parameter_list|()
block|{
return|return
name|simpleWildcard
return|;
block|}
DECL|method|getSegment
name|String
name|getSegment
parameter_list|()
block|{
return|return
name|segment
return|;
block|}
DECL|method|getNext
name|FilterPath
name|getNext
parameter_list|()
block|{
return|return
name|next
return|;
block|}
DECL|method|compile
specifier|public
specifier|static
name|FilterPath
index|[]
name|compile
parameter_list|(
name|String
modifier|...
name|filters
parameter_list|)
block|{
if|if
condition|(
name|CollectionUtils
operator|.
name|isEmpty
argument_list|(
name|filters
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|FilterPath
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|filter
range|:
name|filters
control|)
block|{
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
name|filter
operator|=
name|filter
operator|.
name|trim
argument_list|()
expr_stmt|;
if|if
condition|(
name|filter
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|paths
operator|.
name|add
argument_list|(
name|parse
argument_list|(
name|filter
argument_list|,
name|filter
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|paths
operator|.
name|toArray
argument_list|(
operator|new
name|FilterPath
index|[
name|paths
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
DECL|method|parse
specifier|private
specifier|static
name|FilterPath
name|parse
parameter_list|(
specifier|final
name|String
name|filter
parameter_list|,
specifier|final
name|String
name|segment
parameter_list|)
block|{
name|int
name|end
init|=
name|segment
operator|.
name|length
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
name|end
condition|;
control|)
block|{
name|char
name|c
init|=
name|segment
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|==
literal|'.'
condition|)
block|{
name|String
name|current
init|=
name|segment
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|i
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"\\\\."
argument_list|,
literal|"."
argument_list|)
decl_stmt|;
return|return
operator|new
name|FilterPath
argument_list|(
name|filter
argument_list|,
name|current
argument_list|,
name|parse
argument_list|(
name|filter
argument_list|,
name|segment
operator|.
name|substring
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
operator|++
name|i
expr_stmt|;
if|if
condition|(
operator|(
name|c
operator|==
literal|'\\'
operator|)
operator|&&
operator|(
name|i
operator|<
name|end
operator|)
operator|&&
operator|(
name|segment
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
literal|'.'
operator|)
condition|)
block|{
operator|++
name|i
expr_stmt|;
block|}
block|}
return|return
operator|new
name|FilterPath
argument_list|(
name|filter
argument_list|,
name|segment
operator|.
name|replaceAll
argument_list|(
literal|"\\\\."
argument_list|,
literal|"."
argument_list|)
argument_list|,
name|EMPTY
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
return|return
literal|"FilterPath [filter="
operator|+
name|filter
operator|+
literal|", segment="
operator|+
name|segment
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

