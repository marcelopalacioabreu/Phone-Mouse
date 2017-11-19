public class Parser {
    /**
     * Parse string received from client
     * @param data string
     * @param length length of the data
     */
    public static Actions parse(String data, int length) {
        data = data.substring(0,length);
        String[] dataComponents = data.split(",");

        try {
            return Actions.valueOf(dataComponents[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Expects data to be a move action. Parses velocity after action type
     * @param data string of data
     * @param length of data
     * @return double[3] for velocity
     */
    public static double[] parseVelocity(String data, int length) {
        double[] velocity = new double[3];

        data = data.substring(0,length);
        String[] dataComponents = data.split(",");

        for(int i = 0; i < velocity.length; i++) {
            //dataComponents is +1 to skip action enum
            velocity[i] = Double.parseDouble(dataComponents[i+1]);
        }
        return velocity;
    }
}
