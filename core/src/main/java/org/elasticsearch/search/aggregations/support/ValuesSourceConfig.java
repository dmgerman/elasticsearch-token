begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|support
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|SearchScript
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
name|DocValueFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
import|;
end_import

begin_class
DECL|class|ValuesSourceConfig
specifier|public
class|class
name|ValuesSourceConfig
parameter_list|<
name|VS
extends|extends
name|ValuesSource
parameter_list|>
block|{
DECL|field|valueSourceType
specifier|private
specifier|final
name|ValuesSourceType
name|valueSourceType
decl_stmt|;
DECL|field|fieldContext
specifier|private
name|FieldContext
name|fieldContext
decl_stmt|;
DECL|field|script
specifier|private
name|SearchScript
name|script
decl_stmt|;
DECL|field|scriptValueType
specifier|private
name|ValueType
name|scriptValueType
decl_stmt|;
DECL|field|unmapped
specifier|private
name|boolean
name|unmapped
init|=
literal|false
decl_stmt|;
DECL|field|format
specifier|private
name|DocValueFormat
name|format
init|=
name|DocValueFormat
operator|.
name|RAW
decl_stmt|;
DECL|field|missing
specifier|private
name|Object
name|missing
decl_stmt|;
DECL|field|timeZone
specifier|private
name|DateTimeZone
name|timeZone
decl_stmt|;
DECL|method|ValuesSourceConfig
specifier|public
name|ValuesSourceConfig
parameter_list|(
name|ValuesSourceType
name|valueSourceType
parameter_list|)
block|{
name|this
operator|.
name|valueSourceType
operator|=
name|valueSourceType
expr_stmt|;
block|}
DECL|method|valueSourceType
specifier|public
name|ValuesSourceType
name|valueSourceType
parameter_list|()
block|{
return|return
name|valueSourceType
return|;
block|}
DECL|method|fieldContext
specifier|public
name|FieldContext
name|fieldContext
parameter_list|()
block|{
return|return
name|fieldContext
return|;
block|}
DECL|method|script
specifier|public
name|SearchScript
name|script
parameter_list|()
block|{
return|return
name|script
return|;
block|}
DECL|method|unmapped
specifier|public
name|boolean
name|unmapped
parameter_list|()
block|{
return|return
name|unmapped
return|;
block|}
DECL|method|valid
specifier|public
name|boolean
name|valid
parameter_list|()
block|{
return|return
name|fieldContext
operator|!=
literal|null
operator|||
name|script
operator|!=
literal|null
operator|||
name|unmapped
return|;
block|}
DECL|method|fieldContext
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|fieldContext
parameter_list|(
name|FieldContext
name|fieldContext
parameter_list|)
block|{
name|this
operator|.
name|fieldContext
operator|=
name|fieldContext
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|script
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|script
parameter_list|(
name|SearchScript
name|script
parameter_list|)
block|{
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|scriptValueType
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|scriptValueType
parameter_list|(
name|ValueType
name|scriptValueType
parameter_list|)
block|{
name|this
operator|.
name|scriptValueType
operator|=
name|scriptValueType
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|scriptValueType
specifier|public
name|ValueType
name|scriptValueType
parameter_list|()
block|{
return|return
name|this
operator|.
name|scriptValueType
return|;
block|}
DECL|method|unmapped
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|unmapped
parameter_list|(
name|boolean
name|unmapped
parameter_list|)
block|{
name|this
operator|.
name|unmapped
operator|=
name|unmapped
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|format
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|format
parameter_list|(
specifier|final
name|DocValueFormat
name|format
parameter_list|)
block|{
name|this
operator|.
name|format
operator|=
name|format
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|missing
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|missing
parameter_list|(
specifier|final
name|Object
name|missing
parameter_list|)
block|{
name|this
operator|.
name|missing
operator|=
name|missing
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|missing
specifier|public
name|Object
name|missing
parameter_list|()
block|{
return|return
name|this
operator|.
name|missing
return|;
block|}
DECL|method|timezone
specifier|public
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|timezone
parameter_list|(
specifier|final
name|DateTimeZone
name|timeZone
parameter_list|)
block|{
name|this
operator|.
name|timeZone
operator|=
name|timeZone
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|timezone
specifier|public
name|DateTimeZone
name|timezone
parameter_list|()
block|{
return|return
name|this
operator|.
name|timeZone
return|;
block|}
DECL|method|format
specifier|public
name|DocValueFormat
name|format
parameter_list|()
block|{
return|return
name|format
return|;
block|}
block|}
end_class

end_unit

