begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.slice
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|slice
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
name|search
operator|.
name|Query
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
comment|/**  * An abstract {@link Query} that defines an hash function to partition the documents in multiple slices.  */
end_comment

begin_class
DECL|class|SliceQuery
specifier|public
specifier|abstract
class|class
name|SliceQuery
extends|extends
name|Query
block|{
DECL|field|field
specifier|private
specifier|final
name|String
name|field
decl_stmt|;
DECL|field|id
specifier|private
specifier|final
name|int
name|id
decl_stmt|;
DECL|field|max
specifier|private
specifier|final
name|int
name|max
decl_stmt|;
comment|/**      * @param field The name of the field      * @param id    The id of the slice      * @param max   The maximum number of slices      */
DECL|method|SliceQuery
specifier|public
name|SliceQuery
parameter_list|(
name|String
name|field
parameter_list|,
name|int
name|id
parameter_list|,
name|int
name|max
parameter_list|)
block|{
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|max
operator|=
name|max
expr_stmt|;
block|}
comment|// Returns true if the value matches the predicate
DECL|method|contains
specifier|protected
specifier|final
name|boolean
name|contains
parameter_list|(
name|long
name|value
parameter_list|)
block|{
return|return
name|Math
operator|.
name|floorMod
argument_list|(
name|value
argument_list|,
name|max
argument_list|)
operator|==
name|id
return|;
block|}
DECL|method|getField
specifier|public
name|String
name|getField
parameter_list|()
block|{
return|return
name|field
return|;
block|}
DECL|method|getId
specifier|public
name|int
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
DECL|method|getMax
specifier|public
name|int
name|getMax
parameter_list|()
block|{
return|return
name|max
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
name|o
parameter_list|)
block|{
if|if
condition|(
name|sameClassAs
argument_list|(
name|o
argument_list|)
operator|==
literal|false
condition|)
block|{
return|return
literal|false
return|;
block|}
name|SliceQuery
name|that
init|=
operator|(
name|SliceQuery
operator|)
name|o
decl_stmt|;
return|return
name|field
operator|.
name|equals
argument_list|(
name|that
operator|.
name|field
argument_list|)
operator|&&
name|id
operator|==
name|that
operator|.
name|id
operator|&&
name|max
operator|==
name|that
operator|.
name|max
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
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|classHash
argument_list|()
argument_list|,
name|field
argument_list|,
name|id
argument_list|,
name|max
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|(
name|String
name|f
parameter_list|)
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"[field="
operator|+
name|field
operator|+
literal|", id="
operator|+
name|id
operator|+
literal|", max="
operator|+
name|max
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

