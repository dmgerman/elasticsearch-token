begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.reducers.movavg
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|reducers
operator|.
name|movavg
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
name|search
operator|.
name|aggregations
operator|.
name|reducers
operator|.
name|ReducerBuilder
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
name|aggregations
operator|.
name|reducers
operator|.
name|movavg
operator|.
name|models
operator|.
name|MovAvgModelBuilder
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|reducers
operator|.
name|BucketHelpers
operator|.
name|GapPolicy
import|;
end_import

begin_comment
comment|/**  * A builder to create MovingAvg reducer aggregations  */
end_comment

begin_class
DECL|class|MovAvgBuilder
specifier|public
class|class
name|MovAvgBuilder
extends|extends
name|ReducerBuilder
argument_list|<
name|MovAvgBuilder
argument_list|>
block|{
DECL|field|format
specifier|private
name|String
name|format
decl_stmt|;
DECL|field|gapPolicy
specifier|private
name|GapPolicy
name|gapPolicy
decl_stmt|;
DECL|field|modelBuilder
specifier|private
name|MovAvgModelBuilder
name|modelBuilder
decl_stmt|;
DECL|field|window
specifier|private
name|Integer
name|window
decl_stmt|;
DECL|method|MovAvgBuilder
specifier|public
name|MovAvgBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|MovAvgReducer
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|format
specifier|public
name|MovAvgBuilder
name|format
parameter_list|(
name|String
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
comment|/**      * Defines what should be done when a gap in the series is discovered      *      * @param gapPolicy A GapPolicy enum defining the selected policy      * @return Returns the builder to continue chaining      */
DECL|method|gapPolicy
specifier|public
name|MovAvgBuilder
name|gapPolicy
parameter_list|(
name|GapPolicy
name|gapPolicy
parameter_list|)
block|{
name|this
operator|.
name|gapPolicy
operator|=
name|gapPolicy
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets a MovAvgModelBuilder for the Moving Average.  The model builder is used to      * define what type of moving average you want to use on the series      *      * @param modelBuilder A MovAvgModelBuilder which has been prepopulated with settings      * @return Returns the builder to continue chaining      */
DECL|method|modelBuilder
specifier|public
name|MovAvgBuilder
name|modelBuilder
parameter_list|(
name|MovAvgModelBuilder
name|modelBuilder
parameter_list|)
block|{
name|this
operator|.
name|modelBuilder
operator|=
name|modelBuilder
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the window size for the moving average.  This window will "slide" across the      * series, and the values inside that window will be used to calculate the moving avg value      *      * @param window Size of window      * @return Returns the builder to continue chaining      */
DECL|method|window
specifier|public
name|MovAvgBuilder
name|window
parameter_list|(
name|int
name|window
parameter_list|)
block|{
name|this
operator|.
name|window
operator|=
name|window
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|internalXContent
specifier|protected
name|XContentBuilder
name|internalXContent
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
if|if
condition|(
name|format
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|MovAvgParser
operator|.
name|FORMAT
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|format
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|gapPolicy
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|MovAvgParser
operator|.
name|GAP_POLICY
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|gapPolicy
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|modelBuilder
operator|!=
literal|null
condition|)
block|{
name|modelBuilder
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|window
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|MovAvgParser
operator|.
name|WINDOW
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|window
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

