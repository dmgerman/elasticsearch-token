begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
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
name|logging
operator|.
name|DeprecationLogger
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
name|logging
operator|.
name|Loggers
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * Holds a field that can be found in a request while parsing and its different  * variants, which may be deprecated.  */
end_comment

begin_class
DECL|class|ParseField
specifier|public
class|class
name|ParseField
block|{
DECL|field|DEPRECATION_LOGGER
specifier|private
specifier|static
specifier|final
name|DeprecationLogger
name|DEPRECATION_LOGGER
init|=
operator|new
name|DeprecationLogger
argument_list|(
name|Loggers
operator|.
name|getLogger
argument_list|(
name|ParseField
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|deprecatedNames
specifier|private
specifier|final
name|String
index|[]
name|deprecatedNames
decl_stmt|;
DECL|field|allReplacedWith
specifier|private
name|String
name|allReplacedWith
init|=
literal|null
decl_stmt|;
DECL|field|allNames
specifier|private
specifier|final
name|String
index|[]
name|allNames
decl_stmt|;
comment|/**      * @param name      *            the primary name for this field. This will be returned by      *            {@link #getPreferredName()}      * @param deprecatedNames      *            names for this field which are deprecated and will not be      *            accepted when strict matching is used.      */
DECL|method|ParseField
specifier|public
name|ParseField
parameter_list|(
name|String
name|name
parameter_list|,
name|String
modifier|...
name|deprecatedNames
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
if|if
condition|(
name|deprecatedNames
operator|==
literal|null
operator|||
name|deprecatedNames
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|this
operator|.
name|deprecatedNames
operator|=
name|Strings
operator|.
name|EMPTY_ARRAY
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|HashSet
argument_list|<
name|String
argument_list|>
name|set
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|set
argument_list|,
name|deprecatedNames
argument_list|)
expr_stmt|;
name|this
operator|.
name|deprecatedNames
operator|=
name|set
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|set
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|allNames
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|allNames
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|allNames
argument_list|,
name|this
operator|.
name|deprecatedNames
argument_list|)
expr_stmt|;
name|this
operator|.
name|allNames
operator|=
name|allNames
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|allNames
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**      * @return the preferred name used for this field      */
DECL|method|getPreferredName
specifier|public
name|String
name|getPreferredName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**      * @return All names for this field regardless of whether they are      *         deprecated      */
DECL|method|getAllNamesIncludedDeprecated
specifier|public
name|String
index|[]
name|getAllNamesIncludedDeprecated
parameter_list|()
block|{
return|return
name|allNames
return|;
block|}
comment|/**      * @param deprecatedNames      *            deprecated names to include with the returned      *            {@link ParseField}      * @return a new {@link ParseField} using the preferred name from this one      *         but with the specified deprecated names      */
DECL|method|withDeprecation
specifier|public
name|ParseField
name|withDeprecation
parameter_list|(
name|String
modifier|...
name|deprecatedNames
parameter_list|)
block|{
return|return
operator|new
name|ParseField
argument_list|(
name|this
operator|.
name|name
argument_list|,
name|deprecatedNames
argument_list|)
return|;
block|}
comment|/**      * Return a new ParseField where all field names are deprecated and replaced      * with {@code allReplacedWith}.      */
DECL|method|withAllDeprecated
specifier|public
name|ParseField
name|withAllDeprecated
parameter_list|(
name|String
name|allReplacedWith
parameter_list|)
block|{
name|ParseField
name|parseField
init|=
name|this
operator|.
name|withDeprecation
argument_list|(
name|getAllNamesIncludedDeprecated
argument_list|()
argument_list|)
decl_stmt|;
name|parseField
operator|.
name|allReplacedWith
operator|=
name|allReplacedWith
expr_stmt|;
return|return
name|parseField
return|;
block|}
comment|/**      * @param fieldName      *            the field name to match against this {@link ParseField}      * @param strict      *            if true an exception will be thrown if a deprecated field name      *            is given. If false the deprecated name will be matched but a      *            message will also be logged to the {@link DeprecationLogger}      * @return true if<code>fieldName</code> matches any of the acceptable      *         names for this {@link ParseField}.      */
DECL|method|match
name|boolean
name|match
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|boolean
name|strict
parameter_list|)
block|{
comment|// if this parse field has not been completely deprecated then try to
comment|// match the preferred name
if|if
condition|(
name|allReplacedWith
operator|==
literal|null
operator|&&
name|fieldName
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// Now try to match against one of the deprecated names. Note that if
comment|// the parse field is entirely deprecated (allReplacedWith != null) all
comment|// fields will be in the deprecatedNames array
name|String
name|msg
decl_stmt|;
for|for
control|(
name|String
name|depName
range|:
name|deprecatedNames
control|)
block|{
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
name|depName
argument_list|)
condition|)
block|{
name|msg
operator|=
literal|"Deprecated field ["
operator|+
name|fieldName
operator|+
literal|"] used, expected ["
operator|+
name|name
operator|+
literal|"] instead"
expr_stmt|;
if|if
condition|(
name|allReplacedWith
operator|!=
literal|null
condition|)
block|{
comment|// If the field is entirely deprecated then there is no
comment|// preferred name so instead use the `allReplaceWith`
comment|// message to indicate what should be used instead
name|msg
operator|=
literal|"Deprecated field ["
operator|+
name|fieldName
operator|+
literal|"] used, replaced by ["
operator|+
name|allReplacedWith
operator|+
literal|"]"
expr_stmt|;
block|}
if|if
condition|(
name|strict
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
else|else
block|{
name|DEPRECATION_LOGGER
operator|.
name|deprecated
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
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
name|getPreferredName
argument_list|()
return|;
block|}
comment|/**      * @return the message to use if this {@link ParseField} has been entirely      *         deprecated in favor of something else. This method will return      *<code>null</code> if the ParseField has not been completely      *         deprecated.      */
DECL|method|getAllReplacedWith
specifier|public
name|String
name|getAllReplacedWith
parameter_list|()
block|{
return|return
name|allReplacedWith
return|;
block|}
comment|/**      * @return an array of the names for the {@link ParseField} which are      *         deprecated.      */
DECL|method|getDeprecatedNames
specifier|public
name|String
index|[]
name|getDeprecatedNames
parameter_list|()
block|{
return|return
name|deprecatedNames
return|;
block|}
DECL|class|CommonFields
specifier|public
specifier|static
class|class
name|CommonFields
block|{
DECL|field|FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
DECL|field|FIELDS
specifier|public
specifier|static
specifier|final
name|ParseField
name|FIELDS
init|=
operator|new
name|ParseField
argument_list|(
literal|"fields"
argument_list|)
decl_stmt|;
DECL|field|FORMAT
specifier|public
specifier|static
specifier|final
name|ParseField
name|FORMAT
init|=
operator|new
name|ParseField
argument_list|(
literal|"format"
argument_list|)
decl_stmt|;
DECL|field|MISSING
specifier|public
specifier|static
specifier|final
name|ParseField
name|MISSING
init|=
operator|new
name|ParseField
argument_list|(
literal|"missing"
argument_list|)
decl_stmt|;
DECL|field|TIME_ZONE
specifier|public
specifier|static
specifier|final
name|ParseField
name|TIME_ZONE
init|=
operator|new
name|ParseField
argument_list|(
literal|"time_zone"
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

