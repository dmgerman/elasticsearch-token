begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.documentation
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|documentation
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
name|join
operator|.
name|ScoreMode
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
name|geo
operator|.
name|ShapeRelation
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
name|builders
operator|.
name|CoordinatesBuilder
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
name|builders
operator|.
name|ShapeBuilders
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
name|query
operator|.
name|GeoShapeQueryBuilder
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
name|functionscore
operator|.
name|FunctionScoreQueryBuilder
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
name|functionscore
operator|.
name|FunctionScoreQueryBuilder
operator|.
name|FilterFunctionBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Script
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
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
name|HashMap
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singletonMap
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|boolQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|boostingQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|commonTermsQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|constantScoreQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|disMaxQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|existsQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|functionScoreQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|fuzzyQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|geoBoundingBoxQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|geoDistanceQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|geoPolygonQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|geoShapeQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|idsQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|matchAllQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|matchQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|moreLikeThisQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|multiMatchQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|nestedQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|prefixQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|queryStringQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|rangeQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|regexpQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|scriptQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|simpleQueryStringQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|spanContainingQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|spanFirstQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|spanMultiTermQueryBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|spanNearQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|spanNotQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|spanOrQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|spanTermQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|spanWithinQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|termQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|termsQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|typeQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|wildcardQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|functionscore
operator|.
name|ScoreFunctionBuilders
operator|.
name|exponentialDecayFunction
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|functionscore
operator|.
name|ScoreFunctionBuilders
operator|.
name|randomFunction
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|join
operator|.
name|query
operator|.
name|JoinQueryBuilders
operator|.
name|hasChildQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|join
operator|.
name|query
operator|.
name|JoinQueryBuilders
operator|.
name|hasParentQuery
import|;
end_import

begin_comment
comment|/**  * Examples of using the transport client that are imported into the transport client documentation.  * There are no assertions here because we're mostly concerned with making sure that the examples  * compile and don't throw weird runtime exceptions. Assertions and example data would be nice, but  * that is secondary.  */
end_comment

begin_class
DECL|class|QueryDSLDocumentationTests
specifier|public
class|class
name|QueryDSLDocumentationTests
extends|extends
name|ESTestCase
block|{
DECL|method|testBool
specifier|public
name|void
name|testBool
parameter_list|()
block|{
comment|// tag::bool
name|boolQuery
argument_list|()
operator|.
name|must
argument_list|(
name|termQuery
argument_list|(
literal|"content"
argument_list|,
literal|"test1"
argument_list|)
argument_list|)
comment|//<1>
operator|.
name|must
argument_list|(
name|termQuery
argument_list|(
literal|"content"
argument_list|,
literal|"test4"
argument_list|)
argument_list|)
comment|//<1>
operator|.
name|mustNot
argument_list|(
name|termQuery
argument_list|(
literal|"content"
argument_list|,
literal|"test2"
argument_list|)
argument_list|)
comment|//<2>
operator|.
name|should
argument_list|(
name|termQuery
argument_list|(
literal|"content"
argument_list|,
literal|"test3"
argument_list|)
argument_list|)
comment|//<3>
operator|.
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"content"
argument_list|,
literal|"test5"
argument_list|)
argument_list|)
expr_stmt|;
comment|//<4>
comment|// end::bool
block|}
DECL|method|testBoosting
specifier|public
name|void
name|testBoosting
parameter_list|()
block|{
comment|// tag::boosting
name|boostingQuery
argument_list|(
name|termQuery
argument_list|(
literal|"name"
argument_list|,
literal|"kimchy"
argument_list|)
argument_list|,
comment|//<1>
name|termQuery
argument_list|(
literal|"name"
argument_list|,
literal|"dadoonet"
argument_list|)
argument_list|)
comment|//<2>
operator|.
name|negativeBoost
argument_list|(
literal|0.2f
argument_list|)
expr_stmt|;
comment|//<3>
comment|// end::boosting
block|}
DECL|method|testCommonTerms
specifier|public
name|void
name|testCommonTerms
parameter_list|()
block|{
comment|// tag::common_terms
name|commonTermsQuery
argument_list|(
literal|"name"
argument_list|,
comment|//<1>
literal|"kimchy"
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::common_terms
block|}
DECL|method|testConstantScore
specifier|public
name|void
name|testConstantScore
parameter_list|()
block|{
comment|// tag::constant_score
name|constantScoreQuery
argument_list|(
name|termQuery
argument_list|(
literal|"name"
argument_list|,
literal|"kimchy"
argument_list|)
argument_list|)
comment|//<1>
operator|.
name|boost
argument_list|(
literal|2.0f
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::constant_score
block|}
DECL|method|testDisMax
specifier|public
name|void
name|testDisMax
parameter_list|()
block|{
comment|// tag::dis_max
name|disMaxQuery
argument_list|()
operator|.
name|add
argument_list|(
name|termQuery
argument_list|(
literal|"name"
argument_list|,
literal|"kimchy"
argument_list|)
argument_list|)
comment|//<1>
operator|.
name|add
argument_list|(
name|termQuery
argument_list|(
literal|"name"
argument_list|,
literal|"elasticsearch"
argument_list|)
argument_list|)
comment|//<2>
operator|.
name|boost
argument_list|(
literal|1.2f
argument_list|)
comment|//<3>
operator|.
name|tieBreaker
argument_list|(
literal|0.7f
argument_list|)
expr_stmt|;
comment|//<4>
comment|// end::dis_max
block|}
DECL|method|testExists
specifier|public
name|void
name|testExists
parameter_list|()
block|{
comment|// tag::exists
name|existsQuery
argument_list|(
literal|"name"
argument_list|)
expr_stmt|;
comment|//<1>
comment|// end::exists
block|}
DECL|method|testFunctionScore
specifier|public
name|void
name|testFunctionScore
parameter_list|()
block|{
comment|// tag::function_score
name|FilterFunctionBuilder
index|[]
name|functions
init|=
block|{
operator|new
name|FunctionScoreQueryBuilder
operator|.
name|FilterFunctionBuilder
argument_list|(
name|matchQuery
argument_list|(
literal|"name"
argument_list|,
literal|"kimchy"
argument_list|)
argument_list|,
comment|//<1>
name|randomFunction
argument_list|(
literal|"ABCDEF"
argument_list|)
argument_list|)
block|,
comment|//<2>
operator|new
name|FunctionScoreQueryBuilder
operator|.
name|FilterFunctionBuilder
argument_list|(
name|exponentialDecayFunction
argument_list|(
literal|"age"
argument_list|,
literal|0L
argument_list|,
literal|1L
argument_list|)
argument_list|)
comment|//<3>
block|}
decl_stmt|;
name|functionScoreQuery
argument_list|(
name|functions
argument_list|)
expr_stmt|;
comment|// end::function_score
block|}
DECL|method|testFuzzy
specifier|public
name|void
name|testFuzzy
parameter_list|()
block|{
comment|// tag::fuzzy
name|fuzzyQuery
argument_list|(
literal|"name"
argument_list|,
comment|//<1>
literal|"kimchy"
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::fuzzy
block|}
DECL|method|testGeoBoundingBox
specifier|public
name|void
name|testGeoBoundingBox
parameter_list|()
block|{
comment|// tag::geo_bounding_box
name|geoBoundingBoxQuery
argument_list|(
literal|"pin.location"
argument_list|)
comment|//<1>
operator|.
name|setCorners
argument_list|(
literal|40.73
argument_list|,
operator|-
literal|74.1
argument_list|,
comment|//<2>
literal|40.717
argument_list|,
operator|-
literal|73.99
argument_list|)
expr_stmt|;
comment|//<3>
comment|// end::geo_bounding_box
block|}
DECL|method|testGeoDistance
specifier|public
name|void
name|testGeoDistance
parameter_list|()
block|{
comment|// tag::geo_distance
name|geoDistanceQuery
argument_list|(
literal|"pin.location"
argument_list|)
comment|//<1>
operator|.
name|point
argument_list|(
literal|40
argument_list|,
operator|-
literal|70
argument_list|)
comment|//<2>
operator|.
name|distance
argument_list|(
literal|200
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
expr_stmt|;
comment|//<3>
comment|// end::geo_distance
block|}
DECL|method|testGeoPolygon
specifier|public
name|void
name|testGeoPolygon
parameter_list|()
block|{
comment|// tag::geo_polygon
name|List
argument_list|<
name|GeoPoint
argument_list|>
name|points
init|=
operator|new
name|ArrayList
argument_list|<
name|GeoPoint
argument_list|>
argument_list|()
decl_stmt|;
comment|//<1>
name|points
operator|.
name|add
argument_list|(
operator|new
name|GeoPoint
argument_list|(
literal|40
argument_list|,
operator|-
literal|70
argument_list|)
argument_list|)
expr_stmt|;
name|points
operator|.
name|add
argument_list|(
operator|new
name|GeoPoint
argument_list|(
literal|30
argument_list|,
operator|-
literal|80
argument_list|)
argument_list|)
expr_stmt|;
name|points
operator|.
name|add
argument_list|(
operator|new
name|GeoPoint
argument_list|(
literal|20
argument_list|,
operator|-
literal|90
argument_list|)
argument_list|)
expr_stmt|;
name|geoPolygonQuery
argument_list|(
literal|"pin.location"
argument_list|,
name|points
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::geo_polygon
block|}
DECL|method|testGeoShape
specifier|public
name|void
name|testGeoShape
parameter_list|()
throws|throws
name|IOException
block|{
block|{
comment|// tag::geo_shape
name|GeoShapeQueryBuilder
name|qb
init|=
name|geoShapeQuery
argument_list|(
literal|"pin.location"
argument_list|,
comment|//<1>
name|ShapeBuilders
operator|.
name|newMultiPoint
argument_list|(
comment|//<2>
operator|new
name|CoordinatesBuilder
argument_list|()
operator|.
name|coordinate
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
operator|.
name|coordinate
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
operator|.
name|coordinate
argument_list|(
literal|10
argument_list|,
literal|10
argument_list|)
operator|.
name|coordinate
argument_list|(
literal|10
argument_list|,
literal|0
argument_list|)
operator|.
name|coordinate
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|qb
operator|.
name|relation
argument_list|(
name|ShapeRelation
operator|.
name|WITHIN
argument_list|)
expr_stmt|;
comment|//<3>
comment|// end::geo_shape
block|}
block|{
comment|// tag::indexed_geo_shape
comment|// Using pre-indexed shapes
name|GeoShapeQueryBuilder
name|qb
init|=
name|geoShapeQuery
argument_list|(
literal|"pin.location"
argument_list|,
comment|//<1>
literal|"DEU"
argument_list|,
comment|//<2>
literal|"countries"
argument_list|)
decl_stmt|;
comment|//<3>
name|qb
operator|.
name|relation
argument_list|(
name|ShapeRelation
operator|.
name|WITHIN
argument_list|)
comment|//<4>
operator|.
name|indexedShapeIndex
argument_list|(
literal|"shapes"
argument_list|)
comment|//<5>
operator|.
name|indexedShapePath
argument_list|(
literal|"location"
argument_list|)
expr_stmt|;
comment|//<6>
comment|// end::indexed_geo_shape
block|}
block|}
DECL|method|testHasChild
specifier|public
name|void
name|testHasChild
parameter_list|()
block|{
comment|// tag::has_child
name|hasChildQuery
argument_list|(
literal|"blog_tag"
argument_list|,
comment|//<1>
name|termQuery
argument_list|(
literal|"tag"
argument_list|,
literal|"something"
argument_list|)
argument_list|,
comment|//<2>
name|ScoreMode
operator|.
name|None
argument_list|)
expr_stmt|;
comment|//<3>
comment|// end::has_child
block|}
DECL|method|testHasParent
specifier|public
name|void
name|testHasParent
parameter_list|()
block|{
comment|// tag::has_parent
name|hasParentQuery
argument_list|(
literal|"blog"
argument_list|,
comment|//<1>
name|termQuery
argument_list|(
literal|"tag"
argument_list|,
literal|"something"
argument_list|)
argument_list|,
comment|//<2>
literal|false
argument_list|)
expr_stmt|;
comment|//<3>
comment|// end::has_parent
block|}
DECL|method|testIds
specifier|public
name|void
name|testIds
parameter_list|()
block|{
comment|// tag::ids
name|idsQuery
argument_list|(
literal|"my_type"
argument_list|,
literal|"type2"
argument_list|)
operator|.
name|addIds
argument_list|(
literal|"1"
argument_list|,
literal|"4"
argument_list|,
literal|"100"
argument_list|)
expr_stmt|;
name|idsQuery
argument_list|()
comment|//<1>
operator|.
name|addIds
argument_list|(
literal|"1"
argument_list|,
literal|"4"
argument_list|,
literal|"100"
argument_list|)
expr_stmt|;
comment|// end::ids
block|}
DECL|method|testMatchAll
specifier|public
name|void
name|testMatchAll
parameter_list|()
block|{
comment|// tag::match_all
name|matchAllQuery
argument_list|()
expr_stmt|;
comment|// end::match_all
block|}
DECL|method|testMatch
specifier|public
name|void
name|testMatch
parameter_list|()
block|{
comment|// tag::match
name|matchQuery
argument_list|(
literal|"name"
argument_list|,
comment|//<1>
literal|"kimchy elasticsearch"
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::match
block|}
DECL|method|testMoreLikeThis
specifier|public
name|void
name|testMoreLikeThis
parameter_list|()
block|{
comment|// tag::more_like_this
name|String
index|[]
name|fields
init|=
block|{
literal|"name.first"
block|,
literal|"name.last"
block|}
decl_stmt|;
comment|//<1>
name|String
index|[]
name|texts
init|=
block|{
literal|"text like this one"
block|}
decl_stmt|;
comment|//<2>
name|moreLikeThisQuery
argument_list|(
name|fields
argument_list|,
name|texts
argument_list|,
literal|null
argument_list|)
operator|.
name|minTermFreq
argument_list|(
literal|1
argument_list|)
comment|//<3>
operator|.
name|maxQueryTerms
argument_list|(
literal|12
argument_list|)
expr_stmt|;
comment|//<4>
comment|// end::more_like_this
block|}
DECL|method|testMultiMatch
specifier|public
name|void
name|testMultiMatch
parameter_list|()
block|{
comment|// tag::multi_match
name|multiMatchQuery
argument_list|(
literal|"kimchy elasticsearch"
argument_list|,
comment|//<1>
literal|"user"
argument_list|,
literal|"message"
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::multi_match
block|}
DECL|method|testNested
specifier|public
name|void
name|testNested
parameter_list|()
block|{
comment|// tag::nested
name|nestedQuery
argument_list|(
literal|"obj1"
argument_list|,
comment|//<1>
name|boolQuery
argument_list|()
comment|//<2>
operator|.
name|must
argument_list|(
name|matchQuery
argument_list|(
literal|"obj1.name"
argument_list|,
literal|"blue"
argument_list|)
argument_list|)
operator|.
name|must
argument_list|(
name|rangeQuery
argument_list|(
literal|"obj1.count"
argument_list|)
operator|.
name|gt
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|,
name|ScoreMode
operator|.
name|Avg
argument_list|)
expr_stmt|;
comment|//<3>
comment|// end::nested
block|}
DECL|method|testPrefix
specifier|public
name|void
name|testPrefix
parameter_list|()
block|{
comment|// tag::prefix
name|prefixQuery
argument_list|(
literal|"brand"
argument_list|,
comment|//<1>
literal|"heine"
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::prefix
block|}
DECL|method|testQueryString
specifier|public
name|void
name|testQueryString
parameter_list|()
block|{
comment|// tag::query_string
name|queryStringQuery
argument_list|(
literal|"+kimchy -elasticsearch"
argument_list|)
expr_stmt|;
comment|// end::query_string
block|}
DECL|method|testRange
specifier|public
name|void
name|testRange
parameter_list|()
block|{
comment|// tag::range
name|rangeQuery
argument_list|(
literal|"price"
argument_list|)
comment|//<1>
operator|.
name|from
argument_list|(
literal|5
argument_list|)
comment|//<2>
operator|.
name|to
argument_list|(
literal|10
argument_list|)
comment|//<3>
operator|.
name|includeLower
argument_list|(
literal|true
argument_list|)
comment|//<4>
operator|.
name|includeUpper
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|//<5>
comment|// end::range
comment|// tag::range_simplified
comment|// A simplified form using gte, gt, lt or lte
name|rangeQuery
argument_list|(
literal|"age"
argument_list|)
comment|//<1>
operator|.
name|gte
argument_list|(
literal|"10"
argument_list|)
comment|//<2>
operator|.
name|lt
argument_list|(
literal|"20"
argument_list|)
expr_stmt|;
comment|//<3>
comment|// end::range_simplified
block|}
DECL|method|testRegExp
specifier|public
name|void
name|testRegExp
parameter_list|()
block|{
comment|// tag::regexp
name|regexpQuery
argument_list|(
literal|"name.first"
argument_list|,
comment|//<1>
literal|"s.*y"
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::regexp
block|}
DECL|method|testScript
specifier|public
name|void
name|testScript
parameter_list|()
block|{
comment|// tag::script_inline
name|scriptQuery
argument_list|(
operator|new
name|Script
argument_list|(
literal|"doc['num1'].value> 1"
argument_list|)
comment|//<1>
argument_list|)
expr_stmt|;
comment|// end::script_inline
comment|// tag::script_file
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|parameters
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|parameters
operator|.
name|put
argument_list|(
literal|"param1"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|scriptQuery
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|STORED
argument_list|,
comment|//<1>
literal|"painless"
argument_list|,
comment|//<2>
literal|"myscript"
argument_list|,
comment|//<3>
name|singletonMap
argument_list|(
literal|"param1"
argument_list|,
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|//<4>
comment|// end::script_file
block|}
DECL|method|testSimpleQueryString
specifier|public
name|void
name|testSimpleQueryString
parameter_list|()
block|{
comment|// tag::simple_query_string
name|simpleQueryStringQuery
argument_list|(
literal|"+kimchy -elasticsearch"
argument_list|)
expr_stmt|;
comment|// end::simple_query_string
block|}
DECL|method|testSpanContaining
specifier|public
name|void
name|testSpanContaining
parameter_list|()
block|{
comment|// tag::span_containing
name|spanContainingQuery
argument_list|(
name|spanNearQuery
argument_list|(
name|spanTermQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"bar"
argument_list|)
argument_list|,
literal|5
argument_list|)
comment|//<1>
operator|.
name|addClause
argument_list|(
name|spanTermQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"baz"
argument_list|)
argument_list|)
operator|.
name|inOrder
argument_list|(
literal|true
argument_list|)
argument_list|,
name|spanTermQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::span_containing
block|}
DECL|method|testSpanFirst
specifier|public
name|void
name|testSpanFirst
parameter_list|()
block|{
comment|// tag::span_first
name|spanFirstQuery
argument_list|(
name|spanTermQuery
argument_list|(
literal|"user"
argument_list|,
literal|"kimchy"
argument_list|)
argument_list|,
comment|//<1>
literal|3
comment|//<2>
argument_list|)
expr_stmt|;
comment|// end::span_first
block|}
DECL|method|testSpanMultiTerm
specifier|public
name|void
name|testSpanMultiTerm
parameter_list|()
block|{
comment|// tag::span_multi
name|spanMultiTermQueryBuilder
argument_list|(
name|prefixQuery
argument_list|(
literal|"user"
argument_list|,
literal|"ki"
argument_list|)
argument_list|)
expr_stmt|;
comment|//<1>
comment|// end::span_multi
block|}
DECL|method|testSpanNear
specifier|public
name|void
name|testSpanNear
parameter_list|()
block|{
comment|// tag::span_near
name|spanNearQuery
argument_list|(
name|spanTermQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value1"
argument_list|)
argument_list|,
comment|//<1>
literal|12
argument_list|)
comment|//<2>
operator|.
name|addClause
argument_list|(
name|spanTermQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value2"
argument_list|)
argument_list|)
comment|//<1>
operator|.
name|addClause
argument_list|(
name|spanTermQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value3"
argument_list|)
argument_list|)
comment|//<1>
operator|.
name|inOrder
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|//<3>
comment|// end::span_near
block|}
DECL|method|testSpanNot
specifier|public
name|void
name|testSpanNot
parameter_list|()
block|{
comment|// tag::span_not
name|spanNotQuery
argument_list|(
name|spanTermQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value1"
argument_list|)
argument_list|,
comment|//<1>
name|spanTermQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::span_not
block|}
DECL|method|testSpanOr
specifier|public
name|void
name|testSpanOr
parameter_list|()
block|{
comment|// tag::span_or
name|spanOrQuery
argument_list|(
name|spanTermQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value1"
argument_list|)
argument_list|)
comment|//<1>
operator|.
name|addClause
argument_list|(
name|spanTermQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value2"
argument_list|)
argument_list|)
comment|//<1>
operator|.
name|addClause
argument_list|(
name|spanTermQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value3"
argument_list|)
argument_list|)
expr_stmt|;
comment|//<1>
comment|// end::span_or
block|}
DECL|method|testSpanTerm
specifier|public
name|void
name|testSpanTerm
parameter_list|()
block|{
comment|// tag::span_term
name|spanTermQuery
argument_list|(
literal|"user"
argument_list|,
comment|//<1>
literal|"kimchy"
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::span_term
block|}
DECL|method|testSpanWithin
specifier|public
name|void
name|testSpanWithin
parameter_list|()
block|{
comment|// tag::span_within
name|spanWithinQuery
argument_list|(
name|spanNearQuery
argument_list|(
name|spanTermQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"bar"
argument_list|)
argument_list|,
literal|5
argument_list|)
comment|//<1>
operator|.
name|addClause
argument_list|(
name|spanTermQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"baz"
argument_list|)
argument_list|)
operator|.
name|inOrder
argument_list|(
literal|true
argument_list|)
argument_list|,
name|spanTermQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::span_within
block|}
DECL|method|testTerm
specifier|public
name|void
name|testTerm
parameter_list|()
block|{
comment|// tag::term
name|termQuery
argument_list|(
literal|"name"
argument_list|,
comment|//<1>
literal|"kimchy"
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::term
block|}
DECL|method|testTerms
specifier|public
name|void
name|testTerms
parameter_list|()
block|{
comment|// tag::terms
name|termsQuery
argument_list|(
literal|"tags"
argument_list|,
comment|//<1>
literal|"blue"
argument_list|,
literal|"pill"
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::terms
block|}
DECL|method|testType
specifier|public
name|void
name|testType
parameter_list|()
block|{
comment|// tag::type
name|typeQuery
argument_list|(
literal|"my_type"
argument_list|)
expr_stmt|;
comment|//<1>
comment|// end::type
block|}
DECL|method|testWildcard
specifier|public
name|void
name|testWildcard
parameter_list|()
block|{
comment|// tag::wildcard
name|wildcardQuery
argument_list|(
literal|"user"
argument_list|,
comment|//<1>
literal|"k?mch*"
argument_list|)
expr_stmt|;
comment|//<2>
comment|// end::wildcard
block|}
block|}
end_class

end_unit

