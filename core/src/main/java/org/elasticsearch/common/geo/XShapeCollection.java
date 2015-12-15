begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.geo
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
package|;
end_package

begin_import
import|import
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|context
operator|.
name|SpatialContext
import|;
end_import

begin_import
import|import
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|shape
operator|.
name|Rectangle
import|;
end_import

begin_import
import|import
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|shape
operator|.
name|Shape
import|;
end_import

begin_import
import|import
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|shape
operator|.
name|ShapeCollection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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

begin_comment
comment|/**  * Extends spatial4j ShapeCollection for points_only shape indexing support  */
end_comment

begin_class
DECL|class|XShapeCollection
specifier|public
class|class
name|XShapeCollection
parameter_list|<
name|S
extends|extends
name|Shape
parameter_list|>
extends|extends
name|ShapeCollection
argument_list|<
name|S
argument_list|>
block|{
DECL|field|pointsOnly
specifier|private
name|boolean
name|pointsOnly
init|=
literal|false
decl_stmt|;
DECL|method|XShapeCollection
specifier|public
name|XShapeCollection
parameter_list|(
name|List
argument_list|<
name|S
argument_list|>
name|shapes
parameter_list|,
name|SpatialContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|shapes
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
DECL|method|pointsOnly
specifier|public
name|boolean
name|pointsOnly
parameter_list|()
block|{
return|return
name|this
operator|.
name|pointsOnly
return|;
block|}
DECL|method|setPointsOnly
specifier|public
name|void
name|setPointsOnly
parameter_list|(
name|boolean
name|pointsOnly
parameter_list|)
block|{
name|this
operator|.
name|pointsOnly
operator|=
name|pointsOnly
expr_stmt|;
block|}
block|}
end_class

end_unit

