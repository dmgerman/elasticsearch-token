begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.geodistance
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|geodistance
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|Facet
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_interface
DECL|interface|GeoDistanceFacet
specifier|public
interface|interface
name|GeoDistanceFacet
extends|extends
name|Facet
extends|,
name|Iterable
argument_list|<
name|GeoDistanceFacet
operator|.
name|Entry
argument_list|>
block|{
comment|/**      * The type of the filter facet.      */
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"geo_distance"
decl_stmt|;
comment|/**      * An ordered list of geo distance facet entries.      */
DECL|method|entries
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|()
function_decl|;
comment|/**      * An ordered list of geo distance facet entries.      */
DECL|method|getEntries
name|List
argument_list|<
name|Entry
argument_list|>
name|getEntries
parameter_list|()
function_decl|;
DECL|class|Entry
specifier|public
class|class
name|Entry
block|{
DECL|field|from
name|double
name|from
init|=
name|Double
operator|.
name|NEGATIVE_INFINITY
decl_stmt|;
DECL|field|to
name|double
name|to
init|=
name|Double
operator|.
name|POSITIVE_INFINITY
decl_stmt|;
DECL|field|count
name|long
name|count
decl_stmt|;
DECL|field|totalCount
name|long
name|totalCount
decl_stmt|;
DECL|field|total
name|double
name|total
decl_stmt|;
DECL|field|min
name|double
name|min
init|=
name|Double
operator|.
name|POSITIVE_INFINITY
decl_stmt|;
DECL|field|max
name|double
name|max
init|=
name|Double
operator|.
name|NEGATIVE_INFINITY
decl_stmt|;
comment|/**          * internal field used to see if this entry was already found for a doc          */
DECL|field|foundInDoc
name|boolean
name|foundInDoc
init|=
literal|false
decl_stmt|;
DECL|method|Entry
name|Entry
parameter_list|()
block|{         }
DECL|method|Entry
specifier|public
name|Entry
parameter_list|(
name|double
name|from
parameter_list|,
name|double
name|to
parameter_list|,
name|long
name|count
parameter_list|,
name|long
name|totalCount
parameter_list|,
name|double
name|total
parameter_list|,
name|double
name|min
parameter_list|,
name|double
name|max
parameter_list|)
block|{
name|this
operator|.
name|from
operator|=
name|from
expr_stmt|;
name|this
operator|.
name|to
operator|=
name|to
expr_stmt|;
name|this
operator|.
name|count
operator|=
name|count
expr_stmt|;
name|this
operator|.
name|totalCount
operator|=
name|totalCount
expr_stmt|;
name|this
operator|.
name|total
operator|=
name|total
expr_stmt|;
name|this
operator|.
name|min
operator|=
name|min
expr_stmt|;
name|this
operator|.
name|max
operator|=
name|max
expr_stmt|;
block|}
DECL|method|from
specifier|public
name|double
name|from
parameter_list|()
block|{
return|return
name|this
operator|.
name|from
return|;
block|}
DECL|method|getFrom
specifier|public
name|double
name|getFrom
parameter_list|()
block|{
return|return
name|from
argument_list|()
return|;
block|}
DECL|method|to
specifier|public
name|double
name|to
parameter_list|()
block|{
return|return
name|this
operator|.
name|to
return|;
block|}
DECL|method|getTo
specifier|public
name|double
name|getTo
parameter_list|()
block|{
return|return
name|to
argument_list|()
return|;
block|}
DECL|method|count
specifier|public
name|long
name|count
parameter_list|()
block|{
return|return
name|this
operator|.
name|count
return|;
block|}
DECL|method|getCount
specifier|public
name|long
name|getCount
parameter_list|()
block|{
return|return
name|count
argument_list|()
return|;
block|}
DECL|method|totalCount
specifier|public
name|long
name|totalCount
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalCount
return|;
block|}
DECL|method|getTotalCount
specifier|public
name|long
name|getTotalCount
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalCount
return|;
block|}
DECL|method|total
specifier|public
name|double
name|total
parameter_list|()
block|{
return|return
name|this
operator|.
name|total
return|;
block|}
DECL|method|getTotal
specifier|public
name|double
name|getTotal
parameter_list|()
block|{
return|return
name|total
argument_list|()
return|;
block|}
comment|/**          * The mean of this facet interval.          */
DECL|method|mean
specifier|public
name|double
name|mean
parameter_list|()
block|{
if|if
condition|(
name|totalCount
operator|==
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|total
operator|/
name|totalCount
return|;
block|}
comment|/**          * The mean of this facet interval.          */
DECL|method|getMean
specifier|public
name|double
name|getMean
parameter_list|()
block|{
return|return
name|mean
argument_list|()
return|;
block|}
DECL|method|min
specifier|public
name|double
name|min
parameter_list|()
block|{
return|return
name|this
operator|.
name|min
return|;
block|}
DECL|method|getMin
specifier|public
name|double
name|getMin
parameter_list|()
block|{
return|return
name|this
operator|.
name|min
return|;
block|}
DECL|method|max
specifier|public
name|double
name|max
parameter_list|()
block|{
return|return
name|this
operator|.
name|max
return|;
block|}
DECL|method|getMax
specifier|public
name|double
name|getMax
parameter_list|()
block|{
return|return
name|this
operator|.
name|max
return|;
block|}
block|}
block|}
end_interface

end_unit

