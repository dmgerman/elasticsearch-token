begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.search.geo
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|search
operator|.
name|geo
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
name|index
operator|.
name|IndexReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|LeafReaderContext
import|;
end_import

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
name|ConstantScoreScorer
import|;
end_import

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
name|ConstantScoreWeight
import|;
end_import

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
name|DocIdSetIterator
import|;
end_import

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
name|IndexSearcher
import|;
end_import

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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Scorer
import|;
end_import

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
name|TwoPhaseIterator
import|;
end_import

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
name|Weight
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|NumericUtils
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
name|geo
operator|.
name|GeoDistance
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
name|common
operator|.
name|unit
operator|.
name|DistanceUnit
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
name|fielddata
operator|.
name|IndexGeoPointFieldData
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
name|fielddata
operator|.
name|MultiGeoPointValues
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
name|mapper
operator|.
name|geo
operator|.
name|GeoPointFieldMapperLegacy
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|GeoDistanceRangeQuery
specifier|public
class|class
name|GeoDistanceRangeQuery
extends|extends
name|Query
block|{
DECL|field|lat
specifier|private
specifier|final
name|double
name|lat
decl_stmt|;
DECL|field|lon
specifier|private
specifier|final
name|double
name|lon
decl_stmt|;
DECL|field|inclusiveLowerPoint
specifier|private
specifier|final
name|double
name|inclusiveLowerPoint
decl_stmt|;
comment|// in meters
DECL|field|inclusiveUpperPoint
specifier|private
specifier|final
name|double
name|inclusiveUpperPoint
decl_stmt|;
comment|// in meters
DECL|field|geoDistance
specifier|private
specifier|final
name|GeoDistance
name|geoDistance
decl_stmt|;
DECL|field|fixedSourceDistance
specifier|private
specifier|final
name|GeoDistance
operator|.
name|FixedSourceDistance
name|fixedSourceDistance
decl_stmt|;
DECL|field|distanceBoundingCheck
specifier|private
name|GeoDistance
operator|.
name|DistanceBoundingCheck
name|distanceBoundingCheck
decl_stmt|;
DECL|field|boundingBoxFilter
specifier|private
specifier|final
name|Query
name|boundingBoxFilter
decl_stmt|;
DECL|field|indexFieldData
specifier|private
specifier|final
name|IndexGeoPointFieldData
name|indexFieldData
decl_stmt|;
DECL|method|GeoDistanceRangeQuery
specifier|public
name|GeoDistanceRangeQuery
parameter_list|(
name|GeoPoint
name|point
parameter_list|,
name|Double
name|lowerVal
parameter_list|,
name|Double
name|upperVal
parameter_list|,
name|boolean
name|includeLower
parameter_list|,
name|boolean
name|includeUpper
parameter_list|,
name|GeoDistance
name|geoDistance
parameter_list|,
name|GeoPointFieldMapperLegacy
operator|.
name|GeoPointFieldType
name|fieldType
parameter_list|,
name|IndexGeoPointFieldData
name|indexFieldData
parameter_list|,
name|String
name|optimizeBbox
parameter_list|)
block|{
name|this
operator|.
name|lat
operator|=
name|point
operator|.
name|lat
argument_list|()
expr_stmt|;
name|this
operator|.
name|lon
operator|=
name|point
operator|.
name|lon
argument_list|()
expr_stmt|;
name|this
operator|.
name|geoDistance
operator|=
name|geoDistance
expr_stmt|;
name|this
operator|.
name|indexFieldData
operator|=
name|indexFieldData
expr_stmt|;
name|this
operator|.
name|fixedSourceDistance
operator|=
name|geoDistance
operator|.
name|fixedSourceDistance
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
if|if
condition|(
name|lowerVal
operator|!=
literal|null
condition|)
block|{
name|double
name|f
init|=
name|lowerVal
operator|.
name|doubleValue
argument_list|()
decl_stmt|;
name|long
name|i
init|=
name|NumericUtils
operator|.
name|doubleToSortableLong
argument_list|(
name|f
argument_list|)
decl_stmt|;
name|inclusiveLowerPoint
operator|=
name|NumericUtils
operator|.
name|sortableLongToDouble
argument_list|(
name|includeLower
condition|?
name|i
else|:
operator|(
name|i
operator|+
literal|1L
operator|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|inclusiveLowerPoint
operator|=
name|Double
operator|.
name|NEGATIVE_INFINITY
expr_stmt|;
block|}
if|if
condition|(
name|upperVal
operator|!=
literal|null
condition|)
block|{
name|double
name|f
init|=
name|upperVal
operator|.
name|doubleValue
argument_list|()
decl_stmt|;
name|long
name|i
init|=
name|NumericUtils
operator|.
name|doubleToSortableLong
argument_list|(
name|f
argument_list|)
decl_stmt|;
name|inclusiveUpperPoint
operator|=
name|NumericUtils
operator|.
name|sortableLongToDouble
argument_list|(
name|includeUpper
condition|?
name|i
else|:
operator|(
name|i
operator|-
literal|1L
operator|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|inclusiveUpperPoint
operator|=
name|Double
operator|.
name|POSITIVE_INFINITY
expr_stmt|;
comment|// we disable bounding box in this case, since the upper point is all and we create bounding box up to the
comment|// upper point it will effectively include all
comment|// TODO we can create a bounding box up to from and "not" it
name|optimizeBbox
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|optimizeBbox
operator|!=
literal|null
operator|&&
operator|!
literal|"none"
operator|.
name|equals
argument_list|(
name|optimizeBbox
argument_list|)
condition|)
block|{
name|distanceBoundingCheck
operator|=
name|GeoDistance
operator|.
name|distanceBoundingCheck
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|,
name|inclusiveUpperPoint
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
if|if
condition|(
literal|"memory"
operator|.
name|equals
argument_list|(
name|optimizeBbox
argument_list|)
condition|)
block|{
name|boundingBoxFilter
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"indexed"
operator|.
name|equals
argument_list|(
name|optimizeBbox
argument_list|)
condition|)
block|{
name|boundingBoxFilter
operator|=
name|IndexedGeoBoundingBoxQuery
operator|.
name|create
argument_list|(
name|distanceBoundingCheck
operator|.
name|topLeft
argument_list|()
argument_list|,
name|distanceBoundingCheck
operator|.
name|bottomRight
argument_list|()
argument_list|,
name|fieldType
argument_list|)
expr_stmt|;
name|distanceBoundingCheck
operator|=
name|GeoDistance
operator|.
name|ALWAYS_INSTANCE
expr_stmt|;
comment|// fine, we do the bounding box check using the filter
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"type ["
operator|+
name|optimizeBbox
operator|+
literal|"] for bounding box optimization not supported"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|distanceBoundingCheck
operator|=
name|GeoDistance
operator|.
name|ALWAYS_INSTANCE
expr_stmt|;
name|boundingBoxFilter
operator|=
literal|null
expr_stmt|;
block|}
block|}
DECL|method|lat
specifier|public
name|double
name|lat
parameter_list|()
block|{
return|return
name|lat
return|;
block|}
DECL|method|lon
specifier|public
name|double
name|lon
parameter_list|()
block|{
return|return
name|lon
return|;
block|}
DECL|method|geoDistance
specifier|public
name|GeoDistance
name|geoDistance
parameter_list|()
block|{
return|return
name|geoDistance
return|;
block|}
DECL|method|minInclusiveDistance
specifier|public
name|double
name|minInclusiveDistance
parameter_list|()
block|{
return|return
name|inclusiveLowerPoint
return|;
block|}
DECL|method|maxInclusiveDistance
specifier|public
name|double
name|maxInclusiveDistance
parameter_list|()
block|{
return|return
name|inclusiveUpperPoint
return|;
block|}
DECL|method|fieldName
specifier|public
name|String
name|fieldName
parameter_list|()
block|{
return|return
name|indexFieldData
operator|.
name|getFieldNames
argument_list|()
operator|.
name|indexName
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|rewrite
specifier|public
name|Query
name|rewrite
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|rewrite
argument_list|(
name|reader
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createWeight
specifier|public
name|Weight
name|createWeight
parameter_list|(
name|IndexSearcher
name|searcher
parameter_list|,
name|boolean
name|needsScores
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Weight
name|boundingBoxWeight
decl_stmt|;
if|if
condition|(
name|boundingBoxFilter
operator|!=
literal|null
condition|)
block|{
name|boundingBoxWeight
operator|=
name|searcher
operator|.
name|createNormalizedWeight
argument_list|(
name|boundingBoxFilter
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|boundingBoxWeight
operator|=
literal|null
expr_stmt|;
block|}
return|return
operator|new
name|ConstantScoreWeight
argument_list|(
name|this
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|Scorer
name|scorer
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|DocIdSetIterator
name|approximation
decl_stmt|;
if|if
condition|(
name|boundingBoxWeight
operator|!=
literal|null
condition|)
block|{
name|Scorer
name|s
init|=
name|boundingBoxWeight
operator|.
name|scorer
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|s
operator|==
literal|null
condition|)
block|{
comment|// if the approximation does not match anything, we're done
return|return
literal|null
return|;
block|}
name|approximation
operator|=
name|s
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|approximation
operator|=
name|DocIdSetIterator
operator|.
name|all
argument_list|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|final
name|MultiGeoPointValues
name|values
init|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getGeoPointValues
argument_list|()
decl_stmt|;
specifier|final
name|TwoPhaseIterator
name|twoPhaseIterator
init|=
operator|new
name|TwoPhaseIterator
argument_list|(
name|approximation
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|matches
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|doc
init|=
name|approximation
operator|.
name|docID
argument_list|()
decl_stmt|;
name|values
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
specifier|final
name|int
name|length
init|=
name|values
operator|.
name|count
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
name|length
condition|;
name|i
operator|++
control|)
block|{
name|GeoPoint
name|point
init|=
name|values
operator|.
name|valueAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|distanceBoundingCheck
operator|.
name|isWithin
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|)
condition|)
block|{
name|double
name|d
init|=
name|fixedSourceDistance
operator|.
name|calculate
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|d
operator|>=
name|inclusiveLowerPoint
operator|&&
name|d
operator|<=
name|inclusiveUpperPoint
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|matchCost
parameter_list|()
block|{
if|if
condition|(
name|distanceBoundingCheck
operator|==
name|GeoDistance
operator|.
name|ALWAYS_INSTANCE
condition|)
block|{
return|return
literal|0.0f
return|;
block|}
else|else
block|{
comment|// TODO: is this right (up to 4 comparisons from GeoDistance.SimpleDistanceBoundingCheck)?
return|return
literal|4.0f
return|;
block|}
block|}
block|}
decl_stmt|;
return|return
operator|new
name|ConstantScoreScorer
argument_list|(
name|this
argument_list|,
name|score
argument_list|()
argument_list|,
name|twoPhaseIterator
argument_list|)
return|;
block|}
block|}
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
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|super
operator|.
name|equals
argument_list|(
name|o
argument_list|)
operator|==
literal|false
condition|)
return|return
literal|false
return|;
name|GeoDistanceRangeQuery
name|filter
init|=
operator|(
name|GeoDistanceRangeQuery
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|Double
operator|.
name|compare
argument_list|(
name|filter
operator|.
name|inclusiveLowerPoint
argument_list|,
name|inclusiveLowerPoint
argument_list|)
operator|!=
literal|0
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|Double
operator|.
name|compare
argument_list|(
name|filter
operator|.
name|inclusiveUpperPoint
argument_list|,
name|inclusiveUpperPoint
argument_list|)
operator|!=
literal|0
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|Double
operator|.
name|compare
argument_list|(
name|filter
operator|.
name|lat
argument_list|,
name|lat
argument_list|)
operator|!=
literal|0
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|Double
operator|.
name|compare
argument_list|(
name|filter
operator|.
name|lon
argument_list|,
name|lon
argument_list|)
operator|!=
literal|0
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|indexFieldData
operator|.
name|getFieldNames
argument_list|()
operator|.
name|indexName
argument_list|()
operator|.
name|equals
argument_list|(
name|filter
operator|.
name|indexFieldData
operator|.
name|getFieldNames
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|geoDistance
operator|!=
name|filter
operator|.
name|geoDistance
condition|)
return|return
literal|false
return|;
return|return
literal|true
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
name|field
parameter_list|)
block|{
return|return
literal|"GeoDistanceRangeQuery("
operator|+
name|indexFieldData
operator|.
name|getFieldNames
argument_list|()
operator|.
name|indexName
argument_list|()
operator|+
literal|", "
operator|+
name|geoDistance
operator|+
literal|", ["
operator|+
name|inclusiveLowerPoint
operator|+
literal|" - "
operator|+
name|inclusiveUpperPoint
operator|+
literal|"], "
operator|+
name|lat
operator|+
literal|", "
operator|+
name|lon
operator|+
literal|")"
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
name|int
name|result
init|=
name|super
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|long
name|temp
decl_stmt|;
name|temp
operator|=
name|lat
operator|!=
operator|+
literal|0.0d
condition|?
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|lat
argument_list|)
else|:
literal|0L
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|Long
operator|.
name|hashCode
argument_list|(
name|temp
argument_list|)
expr_stmt|;
name|temp
operator|=
name|lon
operator|!=
operator|+
literal|0.0d
condition|?
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|lon
argument_list|)
else|:
literal|0L
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|Long
operator|.
name|hashCode
argument_list|(
name|temp
argument_list|)
expr_stmt|;
name|temp
operator|=
name|inclusiveLowerPoint
operator|!=
operator|+
literal|0.0d
condition|?
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|inclusiveLowerPoint
argument_list|)
else|:
literal|0L
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|Long
operator|.
name|hashCode
argument_list|(
name|temp
argument_list|)
expr_stmt|;
name|temp
operator|=
name|inclusiveUpperPoint
operator|!=
operator|+
literal|0.0d
condition|?
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|inclusiveUpperPoint
argument_list|)
else|:
literal|0L
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|Long
operator|.
name|hashCode
argument_list|(
name|temp
argument_list|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|geoDistance
operator|!=
literal|null
condition|?
name|geoDistance
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|indexFieldData
operator|.
name|getFieldNames
argument_list|()
operator|.
name|indexName
argument_list|()
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

