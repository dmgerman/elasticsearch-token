begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.geobounds
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|geobounds
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
name|geo
operator|.
name|GeoPoint
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
name|Aggregation
import|;
end_import

begin_comment
comment|/**  * An aggregation that computes a bounding box in which all documents of the current bucket are.  */
end_comment

begin_interface
DECL|interface|GeoBounds
specifier|public
interface|interface
name|GeoBounds
extends|extends
name|Aggregation
block|{
comment|/**      * Get the top-left location of the bounding box.      */
DECL|method|topLeft
name|GeoPoint
name|topLeft
parameter_list|()
function_decl|;
comment|/**      * Get the bottom-right location of the bounding box.      */
DECL|method|bottomRight
name|GeoPoint
name|bottomRight
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

