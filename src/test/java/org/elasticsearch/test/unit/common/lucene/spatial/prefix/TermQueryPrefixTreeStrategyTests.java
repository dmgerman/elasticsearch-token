begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.test.unit.common.lucene.spatial.prefix
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|unit
operator|.
name|common
operator|.
name|lucene
operator|.
name|spatial
operator|.
name|prefix
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|ShapeBuilder
operator|.
name|newPoint
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|ShapeBuilder
operator|.
name|newPolygon
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|ShapeBuilder
operator|.
name|newRectangle
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|testng
operator|.
name|Assert
operator|.
name|assertTrue
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|core
operator|.
name|KeywordAnalyzer
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
name|document
operator|.
name|Document
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
name|document
operator|.
name|Field
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
name|document
operator|.
name|StringField
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
name|IndexWriter
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
name|IndexWriterConfig
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
name|Filter
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
name|MatchAllDocsQuery
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
name|ScoreDoc
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
name|TopDocs
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
name|spatial
operator|.
name|prefix
operator|.
name|TermQueryPrefixTreeStrategy
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
name|spatial
operator|.
name|prefix
operator|.
name|tree
operator|.
name|GeohashPrefixTree
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
name|spatial
operator|.
name|prefix
operator|.
name|tree
operator|.
name|QuadPrefixTree
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
name|spatial
operator|.
name|prefix
operator|.
name|tree
operator|.
name|SpatialPrefixTree
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
name|store
operator|.
name|Directory
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
name|store
operator|.
name|RAMDirectory
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
name|IOUtils
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
name|Version
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
name|GeoShapeConstants
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
name|lucene
operator|.
name|spatial
operator|.
name|XTermQueryPrefixTreeStategy
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
name|FieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|AfterTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|BeforeTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
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

begin_comment
comment|/**  * Tests for {@link TermQueryPrefixTreeStrategy}  */
end_comment

begin_class
DECL|class|TermQueryPrefixTreeStrategyTests
specifier|public
class|class
name|TermQueryPrefixTreeStrategyTests
block|{
comment|// TODO: Randomize the implementation choice
DECL|field|QUAD_PREFIX_TREE
specifier|private
specifier|static
specifier|final
name|SpatialPrefixTree
name|QUAD_PREFIX_TREE
init|=
operator|new
name|QuadPrefixTree
argument_list|(
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|,
name|QuadPrefixTree
operator|.
name|DEFAULT_MAX_LEVELS
argument_list|)
decl_stmt|;
DECL|field|GEOHASH_PREFIX_TREE
specifier|private
specifier|static
specifier|final
name|SpatialPrefixTree
name|GEOHASH_PREFIX_TREE
init|=
operator|new
name|GeohashPrefixTree
argument_list|(
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|,
name|GeohashPrefixTree
operator|.
name|getMaxLevelsPossible
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|STRATEGY
specifier|private
specifier|static
specifier|final
name|XTermQueryPrefixTreeStategy
name|STRATEGY
init|=
operator|new
name|XTermQueryPrefixTreeStategy
argument_list|(
name|GEOHASH_PREFIX_TREE
argument_list|,
operator|new
name|FieldMapper
operator|.
name|Names
argument_list|(
literal|"shape"
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|directory
specifier|private
name|Directory
name|directory
decl_stmt|;
DECL|field|indexReader
specifier|private
name|IndexReader
name|indexReader
decl_stmt|;
DECL|field|indexSearcher
specifier|private
name|IndexSearcher
name|indexSearcher
decl_stmt|;
annotation|@
name|BeforeTest
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|directory
operator|=
operator|new
name|RAMDirectory
argument_list|()
expr_stmt|;
name|IndexWriter
name|writer
init|=
operator|new
name|IndexWriter
argument_list|(
name|directory
argument_list|,
operator|new
name|IndexWriterConfig
argument_list|(
name|Version
operator|.
name|LUCENE_36
argument_list|,
operator|new
name|KeywordAnalyzer
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|newDocument
argument_list|(
literal|"1"
argument_list|,
name|newPoint
argument_list|(
operator|-
literal|30
argument_list|,
operator|-
literal|30
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|newDocument
argument_list|(
literal|"2"
argument_list|,
name|newPoint
argument_list|(
operator|-
literal|45
argument_list|,
operator|-
literal|45
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|newDocument
argument_list|(
literal|"3"
argument_list|,
name|newPoint
argument_list|(
operator|-
literal|45
argument_list|,
literal|50
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|newDocument
argument_list|(
literal|"4"
argument_list|,
name|newRectangle
argument_list|()
operator|.
name|topLeft
argument_list|(
operator|-
literal|50
argument_list|,
literal|50
argument_list|)
operator|.
name|bottomRight
argument_list|(
operator|-
literal|38
argument_list|,
literal|38
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|indexReader
operator|=
name|IndexReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|indexSearcher
operator|=
operator|new
name|IndexSearcher
argument_list|(
name|indexReader
argument_list|)
expr_stmt|;
block|}
DECL|method|newDocument
specifier|private
name|Document
name|newDocument
parameter_list|(
name|String
name|id
parameter_list|,
name|Shape
name|shape
parameter_list|)
block|{
name|Document
name|document
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
literal|"id"
argument_list|,
name|id
argument_list|,
name|StringField
operator|.
name|TYPE_STORED
argument_list|)
argument_list|)
expr_stmt|;
name|Field
index|[]
name|createIndexableFields
init|=
name|STRATEGY
operator|.
name|createIndexableFields
argument_list|(
name|shape
argument_list|)
decl_stmt|;
for|for
control|(
name|Field
name|field
range|:
name|createIndexableFields
control|)
block|{
name|document
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
return|return
name|document
return|;
block|}
DECL|method|assertTopDocs
specifier|private
name|void
name|assertTopDocs
parameter_list|(
name|TopDocs
name|topDocs
parameter_list|,
name|String
modifier|...
name|ids
parameter_list|)
throws|throws
name|IOException
block|{
name|assertTrue
argument_list|(
name|ids
operator|.
name|length
operator|<=
name|topDocs
operator|.
name|totalHits
argument_list|,
literal|"Query has more hits than expected"
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|foundIDs
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ScoreDoc
name|doc
range|:
name|topDocs
operator|.
name|scoreDocs
control|)
block|{
name|Document
name|foundDocument
init|=
name|indexSearcher
operator|.
name|doc
argument_list|(
name|doc
operator|.
name|doc
argument_list|)
decl_stmt|;
name|foundIDs
operator|.
name|add
argument_list|(
name|foundDocument
operator|.
name|getField
argument_list|(
literal|"id"
argument_list|)
operator|.
name|stringValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|id
range|:
name|ids
control|)
block|{
name|assertTrue
argument_list|(
name|foundIDs
operator|.
name|contains
argument_list|(
name|id
argument_list|)
argument_list|,
literal|"ID ["
operator|+
name|id
operator|+
literal|"] was not found in query results"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testIntersectionRelation
specifier|public
name|void
name|testIntersectionRelation
parameter_list|()
throws|throws
name|IOException
block|{
name|Rectangle
name|rectangle
init|=
name|newRectangle
argument_list|()
operator|.
name|topLeft
argument_list|(
operator|-
literal|45
argument_list|,
literal|45
argument_list|)
operator|.
name|bottomRight
argument_list|(
literal|45
argument_list|,
operator|-
literal|45
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Filter
name|filter
init|=
name|STRATEGY
operator|.
name|createIntersectsFilter
argument_list|(
name|rectangle
argument_list|)
decl_stmt|;
name|assertTopDocs
argument_list|(
name|indexSearcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|filter
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"4"
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|STRATEGY
operator|.
name|createIntersectsQuery
argument_list|(
name|rectangle
argument_list|)
decl_stmt|;
name|assertTopDocs
argument_list|(
name|indexSearcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"4"
argument_list|)
expr_stmt|;
name|Shape
name|polygon
init|=
name|newPolygon
argument_list|()
operator|.
name|point
argument_list|(
operator|-
literal|45
argument_list|,
literal|45
argument_list|)
operator|.
name|point
argument_list|(
literal|45
argument_list|,
literal|45
argument_list|)
operator|.
name|point
argument_list|(
literal|45
argument_list|,
operator|-
literal|45
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|45
argument_list|,
operator|-
literal|45
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|45
argument_list|,
literal|45
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|filter
operator|=
name|STRATEGY
operator|.
name|createIntersectsFilter
argument_list|(
name|polygon
argument_list|)
expr_stmt|;
name|assertTopDocs
argument_list|(
name|indexSearcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|filter
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"4"
argument_list|)
expr_stmt|;
name|query
operator|=
name|STRATEGY
operator|.
name|createIntersectsQuery
argument_list|(
name|polygon
argument_list|)
expr_stmt|;
name|assertTopDocs
argument_list|(
name|indexSearcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"4"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testDisjointRelation
specifier|public
name|void
name|testDisjointRelation
parameter_list|()
throws|throws
name|IOException
block|{
name|Rectangle
name|rectangle
init|=
name|newRectangle
argument_list|()
operator|.
name|topLeft
argument_list|(
operator|-
literal|45
argument_list|,
literal|45
argument_list|)
operator|.
name|bottomRight
argument_list|(
literal|45
argument_list|,
operator|-
literal|45
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Filter
name|filter
init|=
name|STRATEGY
operator|.
name|createDisjointFilter
argument_list|(
name|rectangle
argument_list|)
decl_stmt|;
name|assertTopDocs
argument_list|(
name|indexSearcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|filter
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|"3"
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|STRATEGY
operator|.
name|createDisjointQuery
argument_list|(
name|rectangle
argument_list|)
decl_stmt|;
name|assertTopDocs
argument_list|(
name|indexSearcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|"3"
argument_list|)
expr_stmt|;
name|Shape
name|polygon
init|=
name|newPolygon
argument_list|()
operator|.
name|point
argument_list|(
operator|-
literal|45
argument_list|,
literal|45
argument_list|)
operator|.
name|point
argument_list|(
literal|45
argument_list|,
literal|45
argument_list|)
operator|.
name|point
argument_list|(
literal|45
argument_list|,
operator|-
literal|45
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|45
argument_list|,
operator|-
literal|45
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|45
argument_list|,
literal|45
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|filter
operator|=
name|STRATEGY
operator|.
name|createDisjointFilter
argument_list|(
name|polygon
argument_list|)
expr_stmt|;
name|assertTopDocs
argument_list|(
name|indexSearcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|filter
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|"3"
argument_list|)
expr_stmt|;
name|query
operator|=
name|STRATEGY
operator|.
name|createDisjointQuery
argument_list|(
name|polygon
argument_list|)
expr_stmt|;
name|assertTopDocs
argument_list|(
name|indexSearcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|"3"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testWithinRelation
specifier|public
name|void
name|testWithinRelation
parameter_list|()
throws|throws
name|IOException
block|{
name|Rectangle
name|rectangle
init|=
name|newRectangle
argument_list|()
operator|.
name|topLeft
argument_list|(
operator|-
literal|45
argument_list|,
literal|45
argument_list|)
operator|.
name|bottomRight
argument_list|(
literal|45
argument_list|,
operator|-
literal|45
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Filter
name|filter
init|=
name|STRATEGY
operator|.
name|createWithinFilter
argument_list|(
name|rectangle
argument_list|)
decl_stmt|;
name|assertTopDocs
argument_list|(
name|indexSearcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|filter
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|STRATEGY
operator|.
name|createWithinQuery
argument_list|(
name|rectangle
argument_list|)
decl_stmt|;
name|assertTopDocs
argument_list|(
name|indexSearcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|Shape
name|polygon
init|=
name|newPolygon
argument_list|()
operator|.
name|point
argument_list|(
operator|-
literal|45
argument_list|,
literal|45
argument_list|)
operator|.
name|point
argument_list|(
literal|45
argument_list|,
literal|45
argument_list|)
operator|.
name|point
argument_list|(
literal|45
argument_list|,
operator|-
literal|45
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|45
argument_list|,
operator|-
literal|45
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|45
argument_list|,
literal|45
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|filter
operator|=
name|STRATEGY
operator|.
name|createWithinFilter
argument_list|(
name|polygon
argument_list|)
expr_stmt|;
name|assertTopDocs
argument_list|(
name|indexSearcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|filter
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|query
operator|=
name|STRATEGY
operator|.
name|createWithinQuery
argument_list|(
name|polygon
argument_list|)
expr_stmt|;
name|assertTopDocs
argument_list|(
name|indexSearcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterTest
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|IOUtils
operator|.
name|close
argument_list|(
name|indexReader
argument_list|,
name|directory
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

