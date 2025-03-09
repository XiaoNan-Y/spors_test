package com.sports.repository;

import com.sports.entity.TestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TestRecordRepository extends JpaRepository<TestRecord, Long>, JpaSpecificationExecutor<TestRecord> {
<<<<<<< HEAD
    
    @Query("SELECT t FROM TestRecord t WHERE t.student.id = ?1 AND t.sportsItem.id = ?2 AND t.status = 'APPROVED'")
    List<TestRecord> findApprovedRecords(Long studentId, Long sportsItemId);
    
    @Query("SELECT COUNT(t) > 0 FROM TestRecord t WHERE t.student.id = ?1 AND t.sportsItem.id = ?2 AND t.status = 'PENDING'")
    boolean existsPendingRecord(Long studentId, Long sportsItemId);

    // 获取测试总数
    @Query("SELECT COUNT(t) FROM TestRecord t WHERE t.status = 'APPROVED'")
    Long getTestCount();

    // 按项目统计测试数量
    @Query("SELECT NEW map(t.sportsItem.name as itemName, COUNT(t) as count) " +
           "FROM TestRecord t " +
           "WHERE t.status = 'APPROVED' " +
           "GROUP BY t.sportsItem.name")
    List<Object[]> getTestCountByItem();

    // 获取学生平均成绩
    @Query("SELECT NEW map(t.student.realName as studentName, AVG(t.score) as avgScore) " +
           "FROM TestRecord t " +
           "WHERE t.status = 'APPROVED' " +
           "GROUP BY t.student.id, t.student.realName " +
           "ORDER BY AVG(t.score) DESC")
    List<Object[]> getAverageScoreByStudent();

    // 获取某个项目的所有已通过记录
    @Query("SELECT t FROM TestRecord t WHERE t.sportsItem.id = ?1 AND t.status = 'APPROVED'")
    List<TestRecord> findApprovedRecordsByItemId(Long itemId);

    // 获取某个学生的所有已通过记录
    @Query("SELECT t FROM TestRecord t WHERE t.student.id = ?1 AND t.status = 'APPROVED'")
    List<TestRecord> findApprovedRecordsByStudentId(Long studentId);

    // 获取某个教师的所有记录
    @Query("SELECT t FROM TestRecord t WHERE t.teacher.id = ?1")
    List<TestRecord> findByTeacherId(Long teacherId);

    // 统计各状态的记录数
    @Query("SELECT t.status as status, COUNT(t) as count " +
           "FROM TestRecord t " +
           "GROUP BY t.status")
    List<Object[]> countByStatus();
}
=======
    @Query(value = "SELECT COUNT(*) FROM test_record", nativeQuery = true)
    long getTestCount();
    
    @Query(value = "SELECT si.name, COUNT(*) " +
           "FROM test_record tr " +
           "JOIN sports_item si ON tr.sports_item_id = si.id " +
           "GROUP BY si.name", nativeQuery = true)
    List<Object[]> getTestCountByItem();
    
    @Query(value = "SELECT tr.student_id, AVG(tr.score) " +
           "FROM test_record tr " +
           "GROUP BY tr.student_id", nativeQuery = true)
    List<Object[]> getAverageScoreByStudent();
} 
>>>>>>> 9aa70dfb89326d075348adb786f9fca620904233
